package com.signalcollect.fraudppuccino.querylanguage

import scala.io.Source
import com.signalcollect.fraudppuccino.repeatedanalysis._
import com.signalcollect._
import scala.collection.mutable.ArrayBuffer
import language.dynamics
import com.signalcollect.fraudppuccino.structuredetection.BTCTransactionMatcher
import com.signalcollect.fraudppuccino.structuredetection.TransactionAnnouncer
import com.signalcollect.fraudppuccino.structuredetection.DownstreamTransactionPatternEdge
import com.signalcollect.fraudppuccino.structuredetection.UpstreamTransactionPatternEdge
import scala.collection.mutable.HashMap
import com.signalcollect.fraudppuccino.visualization.FraudppuccinoServer
import com.signalcollect.configuration.ActorSystemRegistry
import akka.actor.Props
import com.signalcollect.fraudppuccino.componentdetection.ComponentHandler
import com.signalcollect.fraudppuccino.componentdetection.WorkFlowStep
import com.signalcollect.fraudppuccino.componentdetection.RegisterResultHandler
import com.signalcollect.fraudppuccino.visualization.FraudppuccinoServer
import com.signalcollect.fraudppuccino.componentdetection.CommandLineResultHandler

class QueryExecution {

  val transactions = HashMap[Int, RepeatedAnalysisVertex[Int]]()
  var components: HashMap[Int, Iterable[RepeatedAnalysisVertex[Int]]] = HashMap[Int, Iterable[RepeatedAnalysisVertex[Int]]]()

  val graph = GraphBuilder.withStorageFactory(factory.storage.JavaMapStorage).build
  var iter: Iterator[String] = null

  //Register a component handler
  val system = ActorSystemRegistry.retrieve("SignalCollect").get
  val componentHandler = system.actorOf(Props(new ComponentHandler(graph)), "componentHandler")
  componentHandler ! WorkFlowStep("SIZE > 6")
  componentHandler ! RegisterResultHandler(new FraudppuccinoServer)
  componentHandler ! RegisterResultHandler(CommandLineResultHandler)

  
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

  /**
   * Removes all transactions that have timedout.
   *
   * @param maxTime The max time stamp for unconnected transactions to survive
   */
  def retire(maxTime: Long) {
    val maxComponentTime = maxTime - 1123200
    sendPoisonPillToAllOlderThan(Array(maxTime, maxComponentTime))
  }

  def execute(transactionsAlgorithm: RepeatedAnalysisVertex[_] => VertexAlgorithm) {
    transactions.foreach(tx =>
      if (tx._2 == null) {
        println(transactions)
        println(tx)
      } else {
        tx._2.setAlgorithmImplementation(transactionsAlgorithm)
      })
    graph.recalculateScores
    graph.execute
  }

  def label(transactionsLabel: Option[String] = None, transactionsAlgorithm: RepeatedAnalysisVertex[_] => VertexAlgorithm) {
    execute(transactionsAlgorithm)
    if (transactionsLabel.isDefined) {
      val label = transactionsLabel.get
      transactions.foreach(tx => tx._2.retainState(label))
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

  def sendPoisonPillToAllOlderThan(maxTime: Array[Long]) {
    //Send a time stamped poison pill to all vertices    
    graph.foreachVertexWithGraphEditor(graphEditor => vertex =>
      vertex.deliverSignal(maxTime, None, graphEditor))
  }

  def shutdown = graph.shutdown

}

