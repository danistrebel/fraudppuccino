package com.signalcollect.fraudppuccino.querylanguage

import scala.io.Source
import com.signalcollect.fraudppuccino.repeatedanalysis._
import com.signalcollect._
import com.signalcollect.fraudppuccino.structuredetection.TransactionEdge
import scala.collection.mutable.ArrayBuffer
import language.dynamics
import com.signalcollect.fraudppuccino.evaluation.btc.BTCTransactionMatcher
import com.signalcollect.fraudppuccino.structuredetection.TransactionAnnouncer

class QueryExecution {

  var transactions = ArrayBuffer[RepeatedAnalysisVertex[_]]()
  var senders = ArrayBuffer[RepeatedAnalysisVertex[_]]()

  val graph = GraphBuilder.build
  var iter: Iterator[String] = null

  /**
   * Removes all old transactions from the graph and loads a new window.
   * Assumes that the transactions are ordered by time in the input file.
   *
   * @param filePath The file from where the transactions should be loaded
   * @param startTime The min time from where transactions should be loaded
   * @param endTime The max time up to where transactions should be loaded
   * @param maxTime The max time stamp for unconnected transactions to survive
   * @param matchingAlgorithmFactory Creates the appropriate matching Strategy for the specified usecase
   */
  def load(
    filePath: String,
    startTime: Long,
    endTime: Long,
    maxTime: Long = Long.MaxValue,
    matchingAlgorithmFactory: RepeatedAnalysisVertex[_] => VertexAlgorithm = v => BTCTransactionMatcher(v)) {

    //Send a time stamped poison pill to all vertices
    graph.foreachVertexWithGraphEditor(graphEditor => vertex => vertex.deliverSignal(maxTime, None, graphEditor))

    if (iter == null) {
      iter = Source.fromFile(filePath).getLines
    }

    while (iter.hasNext) {

      val splitted = iter.next.split(",")

      if (splitted(5).toLong >= endTime) {
        return
      }

      if (splitted(5).toLong >= startTime && splitted(2).toInt != splitted(3).toInt) {

        val transaction = new RepeatedAnalysisVertex(splitted(0).toInt * -1)
        transactions += transaction
        transaction.storeAttribute("value", splitted(4).toLong)
        transaction.storeAttribute("time", splitted(5).toLong)
        transaction.storeAttribute("src", splitted(2).toInt)
        transaction.storeAttribute("target", splitted(3).toInt)
        transaction.setAlgorithmImplementation(v => TransactionAnnouncer(v))

        val sender = new RepeatedAnalysisVertex(splitted(2).toInt)
        sender.setAlgorithmImplementation(matchingAlgorithmFactory)
        val receiver = new RepeatedAnalysisVertex(splitted(3).toInt)
        receiver.setAlgorithmImplementation(matchingAlgorithmFactory)
        senders += sender
        senders += receiver

        graph.addVertex(transaction)
        graph.addVertex(sender)
        graph.addVertex(receiver)
      }
    }
  }

  def execute(transactionsAlgorithm: RepeatedAnalysisVertex[_] => VertexAlgorithm, sendersAlgorithm: RepeatedAnalysisVertex[_] => VertexAlgorithm) {
    transactions.foreach(_.setAlgorithmImplementation(transactionsAlgorithm))
    senders.foreach(_.setAlgorithmImplementation(sendersAlgorithm))
    graph.recalculateScores
    graph.execute
  }

  def label(transactionsLabel: Option[String] = None, sendersLabel: Option[String] = None, transactionsAlgorithm: RepeatedAnalysisVertex[_] => VertexAlgorithm, sendersAlgorithm: RepeatedAnalysisVertex[_] => VertexAlgorithm) {
    execute(transactionsAlgorithm, sendersAlgorithm)
    if (transactionsLabel.isDefined) {
      val label = transactionsLabel.get
      transactions.foreach(_.retainState(label))
    }
    if (sendersLabel.isDefined) {
      val label = sendersLabel.get
      senders.foreach(_.retainState(label))
    }
  }

  def shutdown = graph.shutdown

}

