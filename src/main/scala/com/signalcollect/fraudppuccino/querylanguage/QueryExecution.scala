package com.signalcollect.fraudppuccino.querylanguage

import scala.io.Source
import com.signalcollect.fraudppuccino.repeatedanalysis._
import com.signalcollect._
import com.signalcollect.fraudppuccino.structuredetection.TransactionEdge
import scala.collection.mutable.ArrayBuffer
import language.dynamics
import com.signalcollect.fraudppuccino.evaluation.btc.BTCTransactionMatcher
import com.signalcollect.fraudppuccino.structuredetection.TransactionAnnouncer
import com.signalcollect.fraudppuccino.structuredetection.DownstreamTransactionPatternEdge
import com.signalcollect.fraudppuccino.structuredetection.UpstreamTransactionPatternEdge

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
    maxTime: Long = Long.MaxValue) {

    //Send the max age that vertices are allowed to have. 
    sendPoisonPillToAllOlderThan(maxTime)
    graph.execute

    //remove all unconnected transactions that have timed out
    graph.foreachVertex(v =>
      if (v.state == true) {
        val vertex = v.asInstanceOf[RepeatedAnalysisVertex[_]]
        if (vertex.outgoingEdges.exists(edge => edge._2 == DownstreamTransactionPatternEdge || edge._2 == UpstreamTransactionPatternEdge)) {
          vertex.removeAlgorithmImplementation
          transactions += vertex
        } else {
          graph.removeVertex(vertex.id)
        }
      })

    if (iter == null) {
      iter = Source.fromFile(filePath).getLines
    }

    while (iter.hasNext) {

      val splitted = iter.next.split(",")

      if (splitted(5).toLong >= endTime) {
        graph.recalculateScores
        return
      }

      if (splitted(5).toLong >= startTime && splitted(2).toInt != splitted(3).toInt) {
        loadTransaction(splitted(0).toInt * -1, splitted(4).toLong, splitted(5).toLong, splitted(2).toInt, splitted(3).toInt)
      }
    }
    graph.recalculateScores
  }

  def execute(transactionsAlgorithm: RepeatedAnalysisVertex[_] => VertexAlgorithm) {
    transactions.foreach(_.setAlgorithmImplementation(transactionsAlgorithm))
    graph.recalculateScores
    graph.execute
  }

  def label(transactionsLabel: Option[String] = None, sendersLabel: Option[String] = None, transactionsAlgorithm: RepeatedAnalysisVertex[_] => VertexAlgorithm, sendersAlgorithm: RepeatedAnalysisVertex[_] => VertexAlgorithm) {
    execute(transactionsAlgorithm)
    if (transactionsLabel.isDefined) {
      val label = transactionsLabel.get
      transactions.foreach(_.retainState(label))
    }
    if (sendersLabel.isDefined) {
      val label = sendersLabel.get
      senders.foreach(_.retainState(label))
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
    //senders += sender
    //senders += receiver

    graph.addVertex(transaction)
    graph.addVertex(sender)
    graph.addVertex(receiver)
  }

  def sendPoisonPillToAllOlderThan(maxTime: Long) {
    //Send a time stamped poison pill to all vertices
    graph.foreachVertexWithGraphEditor(graphEditor => vertex => vertex.deliverSignal(maxTime, None, graphEditor))
  }

  def shutdown = graph.shutdown

}

