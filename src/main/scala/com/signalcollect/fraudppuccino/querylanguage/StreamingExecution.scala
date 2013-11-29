package com.signalcollect.fraudppuccino.querylanguage

import com.signalcollect.GraphBuilder
import com.signalcollect.configuration.ActorSystemRegistry
import com.signalcollect.fraudppuccino.componentdetection._
import com.signalcollect.fraudppuccino.resulthandling._
import com.signalcollect.factory.storage.JavaMapStorage
import scala.io.Source
import akka.actor.Props
import com.signalcollect.fraudppuccino.repeatedanalysis._
import com.signalcollect.fraudppuccino.componentdetection._
import com.signalcollect.fraudppuccino.structuredetection._
import com.signalcollect.Vertex
import akka.actor.ActorRef
import com.signalcollect.fraudppuccino.componentanalysis._
import java.lang.management.ManagementFactory
import scala.collection.JavaConversions._ 

/**
 * A streamed execution that reads transactions from an input source and matches them
 * according to the conditions specified by the user.
 */ 
case class StreamingExecution(
  sourceFile: String = "", //Path to the input source file
  startTime: Long = 0l, //Unix time stamp
  endTime: Long = 0l, //Unix time stamp
  windowSize: Long = 0l, //in s
  maxTxInterval: Long = 0l, // in s
  exhaustiveMatching: Boolean = true, // should the matcher consider more than one possible matching combination
  matchingComplexity: Integer = 10, //number Of inputs and outputs that are considered in the matching
  matchingMode: MatchingMode = MATCH_ALL, //what kinds of transaction associations should be captured
  maxComponentDuration: Long = 0l, //maximum length of a component
  filters: Iterable[String] = List(), //List of filters that components have to comply with in order to be reported
  resultHandlers: Iterable[String] = List(), // handlers that receive the reported components
  debug: Boolean = false, // prints additional information about the execution if true
  transactionAttributes: Map[String, (Int, String => Any)] = Map[String, (Int, String => Any)]()) //Attributes that should be parsed from the input file. Format (Name, (Index, Parser))
  {
  val graph = GraphBuilder.withStorageFactory(JavaMapStorage).build
  var iter: Iterator[String] = null //Specify the work flow
  val mandatoryTransactionAttributes = Array[String]("id", "src", "target", "time")
  val indexOfId = transactionAttributes("id")._1
  val indexOfTime = transactionAttributes("time")._1
  val indexOfSrc = transactionAttributes("src")._1
  val indexOfTarget = transactionAttributes("target")._1
  var handlerRef: ActorRef = null

  val optionalAttributes = transactionAttributes.filter(attribute => !mandatoryTransactionAttributes.contains(attribute._1))

  val transactionAlgorithm: RepeatedAnalysisVertex[_] => VertexAlgorithm = if (exhaustiveMatching) v => TransactionAnnouncer(v) else v => new UnsubscribingTransactionAnnouncer(v)
  val matcherAlgorithm: RepeatedAnalysisVertex[_] => VertexAlgorithm = if (exhaustiveMatching) v => BTCTransactionMatcher(v, matchingMode, matchingComplexity) else v => GreedyBitcoinMatcher(v, matchingMode, matchingComplexity)

  def execute = {

    val system = ActorSystemRegistry.retrieve("SignalCollect").get
    handlerRef = system.actorOf(Props(new ComponentHandler(graph)), "componentHandler")
    for (handler <- resultHandlers) {
      handlerRef ! RegisterResultHandler(ComponentResultHandler(handler))
    }

    for (filter <- filters) {
      handlerRef ! WorkFlowStep(filter)
    }

    sendStatusUpdate("update", "computation has started")

    for (lowerWindowBound <- startTime to endTime by windowSize) {
      
      val stepStartTime = System.currentTimeMillis
      
      if (maxComponentDuration > 0) { //signal maxComponentDuration if it is set
        retire(Array(lowerWindowBound - maxTxInterval, lowerWindowBound - 2 * maxTxInterval, lowerWindowBound - maxComponentDuration))
      } else {
        retire(Array(lowerWindowBound - maxTxInterval, lowerWindowBound - 2 * maxTxInterval))

      }

      val loadingStartTime = System.currentTimeMillis
      load(sourceFile, lowerWindowBound, lowerWindowBound + windowSize)
      val loadingTime = System.currentTimeMillis - loadingStartTime
      
      sendStatusUpdate("progress", ((lowerWindowBound - startTime) * 100 / (endTime - startTime)).toString)
      
      val executionStartTime = System.currentTimeMillis
      graph.recalculateScores
      graph.execute
      val executionTime = System.currentTimeMillis - executionStartTime
      val stepTime =  System.currentTimeMillis - stepStartTime
      
      if (debug) {
        val memoryBean = ManagementFactory.getMemoryMXBean
        val heapUsage = memoryBean.getHeapMemoryUsage.getUsed/1048576
        val nonHeapUsage = memoryBean.getNonHeapMemoryUsage.getUsed/1048576
        val totalMemoryUsage = heapUsage+nonHeapUsage
        
        println(loadingTime + "," + executionTime + "," + stepTime + "," + lowerWindowBound + "," + heapUsage + "," + nonHeapUsage + "," + totalMemoryUsage +","+ timeInGC)
      }
    }
  }
    
  def timeInGC = {
    ManagementFactory.getGarbageCollectorMXBeans.map(_.getCollectionTime).sum
  }

  /**
   * Loads a new window of transactions.
   * Assumes that the transactions are ordered by time in the input file.
   *
   * @param filePath The file from where the transactions should be loaded
   * @param startTime The min time from where transactions should be loaded
   * @param endTime The max time up to where transactions should be loaded
   *
   */
  def load(
    filePath: String,
    startTime: Long,
    endTime: Long) {

    if (iter == null) {
      iter = Source.fromFile(filePath).getLines
    }

    while (iter.hasNext) {

      val splitted = iter.next.split(",")

      if (splitted(indexOfTime).toLong >= endTime) {
        if (debug) {
          print(splitted(indexOfId) + ",")

        }
        return
      }

      if (splitted(indexOfTime).toLong >= startTime && splitted(indexOfSrc).toInt != splitted(indexOfTarget).toInt) {
        loadTransaction(splitted)
      }
    }
  }

  /**
   * Creates the required graph elements for the transaction
   * i.e. a node for the transaction and nodes for the source and target account of the transaction.
   */
  def loadTransaction(transactionAttributes: IndexedSeq[String]) {
    val transaction = new RepeatedAnalysisVertex(transactionAttributes(indexOfId).toInt * (-1))
    val srcId = transactionAttributes(indexOfSrc).toInt
    val targetId = transactionAttributes(indexOfTarget).toInt
    transaction.storeAttribute("time", transactionAttributes(indexOfTime).toLong)
    transaction.storeAttribute("src", srcId)
    transaction.storeAttribute("target", targetId)

    for (attribute <- optionalAttributes) {
      val attributeValue = attribute._2._2(transactionAttributes(attribute._2._1))
      transaction.storeAttribute(attribute._1, attributeValue)
    }
    
    transaction.setAlgorithmImplementation(transactionAlgorithm)

    val sender = new RepeatedAnalysisVertex(srcId)
    sender.setAlgorithmImplementation(matcherAlgorithm)
    val receiver = new RepeatedAnalysisVertex(targetId)
    receiver.setAlgorithmImplementation(matcherAlgorithm)

    graph.addVertex(transaction)
    graph.addVertex(sender)
    graph.addVertex(receiver)
  }

  /**
   * Sends a timeout to all vertices in the graph
   *
   * @param txTimeout transactions with a lower time stamp that this value are excluded from further matching with other transactions
   * @param componentTimeout components where the highest time stamp is lower than this value report themselves for further processing.
   */
  def retire(timeout: Array[Long]) {
    graph.foreachVertexWithGraphEditor(graphEditor => vertex =>
      vertex.deliverSignal(timeout, None, graphEditor))
    graph.awaitIdle
  }

  def sendStatusUpdate(status: String, msg: String) {
    handlerRef ! ComputationStatus("{\"status\":\"" + status + "\", \"msg\":\"" + msg + "\"}")
  }
}

abstract class MatchingMode

case object MATCH_ALL extends MatchingMode
case object MATCH_CHAIN extends MatchingMode
case object MATCH_AGGREGATION extends MatchingMode
case object MATCH_SPLIT extends MatchingMode

