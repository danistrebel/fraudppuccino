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

case class StreamingExecution(
  sourceFile: String = "",
  startTime: Long = 0l, //Unix time stamp
  endTime: Long = 0l, //Unix time stamp
  windowSize: Long = 0l, //in s
  maxTxInterval: Long = 0l, // in s
  exhaustiveMatching: Boolean = true,// 
  filters: Iterable[String] = List(),
  resultHandlers: Iterable[String] = List(),
  debug: Iterable[String] = List(),
  transactionAttributes: Map[String, (Int, String => Any)] = Map[String, (Int, String => Any)]()) {

  val graph = GraphBuilder.withStorageFactory(JavaMapStorage).build
  var iter: Iterator[String] = null //Specify the work flow

  val mandatoryTransactionAttributes = Array[String]("id", "src", "target", "time")
  val indexOfId = transactionAttributes("id")._1
  val indexOfTime = transactionAttributes("time")._1
  val indexOfSrc = transactionAttributes("src")._1
  val indexOfTarget =  transactionAttributes("target")._1
  
  val optionalAttributes = transactionAttributes.filter(attribute => !mandatoryTransactionAttributes.contains(attribute._1))
  
  val transactionAlgorithm: RepeatedAnalysisVertex[_] => VertexAlgorithm = if(exhaustiveMatching) v => TransactionAnnouncer(v) else v => new UnsubscribingTransactionAnnouncer(v)
  val matcherAlgorithm: RepeatedAnalysisVertex[_] => VertexAlgorithm = if(exhaustiveMatching) v => BTCTransactionMatcher(v) else v => GreedyBitcoinMatcher(v)

  
  def execute = {

    val system = ActorSystemRegistry.retrieve("SignalCollect").get
    val handlerRef = system.actorOf(Props(new ComponentHandler(graph)), "componentHandler")
    for (filter <- filters) {
      handlerRef ! WorkFlowStep(filter)
    }

    for (handler <- resultHandlers) {
      handlerRef ! RegisterResultHandler(ComponentResultHandler(handler))
    }

    for (lowerWindowBound <- startTime to endTime by windowSize) {
      retire(lowerWindowBound - maxTxInterval, lowerWindowBound - maxTxInterval - 1123200)

      load(sourceFile, lowerWindowBound, lowerWindowBound + windowSize)

      val startTime = System.currentTimeMillis
      graph.recalculateScores
      graph.execute
      val executionTime = System.currentTimeMillis - startTime
      println(executionTime + "," + lowerWindowBound)
    }

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
        print(splitted(indexOfId) + ",")
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
    val transaction = new RepeatedAnalysisVertex(transactionAttributes(indexOfId).toInt*(-1))
    val srcId = transactionAttributes(indexOfSrc).toInt
    val targetId = transactionAttributes(indexOfTarget).toInt
    transaction.storeAttribute("time", transactionAttributes(indexOfTime).toLong)
    transaction.storeAttribute("src", srcId)
    transaction.storeAttribute("target", targetId)
    
    for(attribute <- optionalAttributes) {
      val attributeValue = attribute._2._2(transactionAttributes(attribute._2._1))
      transaction.storeAttribute(attribute._1, attributeValue)
    }
        
    transaction.storeAttribute("xCountry", srcId % 10 == targetId % 10) //true of the transaction spans 2 countries.
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
  def retire(txTimeout: Long, componentTimeout: Long) {
    val timeout = Array[Long](txTimeout, componentTimeout)
    graph.foreachVertexWithGraphEditor(graphEditor => vertex =>
      vertex.deliverSignal(timeout, None, graphEditor))
    graph.awaitIdle
  }
}


