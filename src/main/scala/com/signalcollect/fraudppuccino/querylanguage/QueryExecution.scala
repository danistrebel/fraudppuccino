package com.signalcollect.fraudppuccino.querylanguage

import scala.io.Source
import com.signalcollect.fraudppuccino.repeatedanalysis._
import com.signalcollect._
import com.signalcollect.fraudppuccino.structuredetection.TransactionEdge
import com.signalcollect.fraudppuccino.structuredetection.SignalBroadcaster
import com.signalcollect.fraudppuccino.structuredetection.TransactionLinker
import scala.collection.mutable.ArrayBuffer
import language.dynamics

class QueryExecution {

  val transactions = ArrayBuffer[RepeatedAnalysisVertex[_]]()
  val senders = ArrayBuffer[RepeatedAnalysisVertex[_]]()

  val graph = GraphBuilder.build

  def load(filePath: String, start: Int, end: Int) {
    for (line <- Source.fromFile(filePath).getLines) {
      val splitted = line.split(",")

      if (splitted(0).toInt >= end) {
        return
      }

      if (splitted(0).toInt >= start && splitted(2).toInt != splitted(3).toInt) {
        val transaction = new RepeatedAnalysisVertex(splitted(0).toInt * -1)
        transactions += transaction
        val sender = new RepeatedAnalysisVertex(splitted(2).toInt)
        val receiver = new RepeatedAnalysisVertex(splitted(3).toInt)
        senders += sender
        senders += receiver

        transaction.storeAttribute("value", splitted(4).toLong)
        transaction.storeAttribute("time", splitted(5).toLong)
        transaction.storeAttribute("src", splitted(2).toInt)
        transaction.storeAttribute("target", splitted(3).toInt)

        graph.addVertex(transaction)
        graph.addVertex(sender)
        graph.addVertex(receiver)

        graph.addEdge(splitted(0).toInt * -1, new TransactionEdge(splitted(2).toInt))
        graph.addEdge(splitted(3).toInt, new TransactionEdge(splitted(0).toInt * -1))
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

}

