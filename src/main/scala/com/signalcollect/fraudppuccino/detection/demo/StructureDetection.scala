package com.signalcollect.fraudppuccino.detection.demo

import com.signalcollect.Graph
import scala.collection.mutable.Map
import com.signalcollect.fraudppuccino.detection._
import com.signalcollect.fraudppuccino.repeatedanalysis._

class StructureDetection(baseGraph: Graph[Any, Any]) {

  var count = -1
  val transactions = Map[Int, RepeatedAnalysisVertex[_]]()

  val transactionLinking: RepeatedAnalysisVertex[_] => VertexAlgorithm = vertex => new TransactionLinker(vertex)

  def addTransaction(sourceId: Int, targetId: Int, value: Double, time: Long) {
    val transactionVertex = new RepeatedAnalysisVertex(count)
    transactionVertex.storeAttribute("value", value)
    transactionVertex.storeAttribute("time", time)
    transactionVertex.storeAttribute("src", sourceId)
    transactionVertex.storeAttribute("target", targetId)
    transactionVertex.setAlgorithmImplementation(transactionLinking)
    count -= 1
    transactions.put(transactionVertex.id, transactionVertex)
    baseGraph.addVertex(transactionVertex)
    baseGraph.addEdge(sourceId, new TransactionEdge(transactionVertex.id))
    baseGraph.addEdge(transactionVertex.id, new TransactionEdge(targetId.asInstanceOf[Int]))
  }

  def runStructureDetection = {
    baseGraph.recalculateScores
    println(baseGraph.execute)
  }

  def pruneSearchResults = {}

  def retainSubgraphs = {
    val matchedTransactions = transactions.values.filter(_.outgoingEdges.exists(_._2.isInstanceOf[TransactionPatternEdge]))
    //    for (tx <- matchedTransactions) {
    //      println(tx.getResult("src").get + " " + tx.getResult("target").get + " " + tx.getResult("value").get + " " + tx.getResult("time").get)
    //    }
    println("Ratio connected: " + (matchedTransactions.size.toDouble / transactions.size))
  }

  def unloadTimeWindow = {
    baseGraph.foreachVertex(_.removeAllEdges(null))

    for (transactionId <- transactions.map(_._1)) {
      baseGraph.removeVertex(transactionId)
    }
    
    transactions.clear
    count = -1
    baseGraph.awaitIdle
  }
}