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


case class StreamingExecution(
  sourceFile: String = "",
  startTime: Long = 0l, //Unix time stamp
  endTime: Long = 0l, //Unix time stamp
  windowSize: Long = 0l, //in s
  maxTxInterval: Long = 0l, // in s
  filters: Iterable[String] = List(),
  resultHandlers: Iterable[String] = List(),
  debug: Iterable[String] = List()) {

  def SOURCE(src: String) = this.copy(sourceFile = src)
  def START(start: Long) = this.copy(startTime = start)
  def END(end: Long) = this.copy(endTime = end)
  def WINDOWSIZE(size: Long) = this.copy(windowSize = size)
  def TXINTERVAL(length: Long) = this.copy(maxTxInterval = length)
  def FILTER(conditions: Iterable[String]) = this.copy(filters = conditions)
  def RESULTHANDER(handlers: Iterable[String]) = this.copy(resultHandlers = handlers)
  def DEBUG(debuggers: Iterable[String]) = this.copy(debug = debuggers)

  val graph = GraphBuilder.withStorageFactory(JavaMapStorage).build
  var iter: Iterator[String] = null //Specify the work flow

  def execute = {

    val system = ActorSystemRegistry.retrieve("SignalCollect").get
    val handlerRef = system.actorOf(Props(new ComponentHandler(graph)), "componentHandler")
    for (filter <- filters) {
      handlerRef ! WorkFlowStep(filter)
    }
    
    for(handler <- resultHandlers) {
      handlerRef ! RegisterResultHandler(ComponentResultHandler(handler))
    }
        
    for (lowerWindowBound <- startTime to endTime by windowSize) {
      retire(lowerWindowBound-maxTxInterval, lowerWindowBound-maxTxInterval-1123200)
      load(sourceFile, lowerWindowBound, lowerWindowBound + windowSize)
      graph.recalculateScores
      graph.execute
      println(lowerWindowBound)
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

      if (splitted(5).toLong >= endTime) {
        print(splitted(0) + ",")
        return
      }

      if (splitted(5).toLong >= startTime && splitted(2).toInt != splitted(3).toInt) {
        loadTransaction(splitted(0).toInt * -1, splitted(4).toLong, splitted(5).toLong, splitted(2).toInt, splitted(3).toInt)
      }
    }
  }
  
  def loadTransaction(transactionId: Int, value: Long, time: Long, srcId: Int, targetId: Int) {
    val transaction = new RepeatedAnalysisVertex(transactionId)
    transaction.storeAttribute("value", value)
    transaction.storeAttribute("time", time)
    transaction.storeAttribute("src", srcId)
    transaction.storeAttribute("target", targetId)
    transaction.setAlgorithmImplementation(v => TransactionAnnouncer(v))

    val sender = new RepeatedAnalysisVertex(srcId)
    sender.setAlgorithmImplementation(v => BTCTransactionMatcher(v))
    val receiver = new RepeatedAnalysisVertex(targetId)
    receiver.setAlgorithmImplementation(v => BTCTransactionMatcher(v))

    graph.addVertex(transaction)
    graph.addVertex(sender)
    graph.addVertex(receiver)
  }
  
  def retire(txTimeout: Long, componentTimeout: Long) {
    graph.foreachVertexWithGraphEditor(graphEditor => vertex =>
      vertex.deliverSignal(Array[Long](txTimeout, componentTimeout), None, graphEditor))
  }
}


