package com.signalcollect.fraudppuccino.detection.demo

import com.signalcollect.fraudppuccino.repeatedanalysis._
import com.signalcollect.GraphBuilder
import scala.io.Source
import scala.collection.mutable.Map
import com.signalcollect.fraudppuccino.repeatedanalysis._
import com.signalcollect.fraudppuccino.detection._
import com.signalcollect.fraudppuccino.structuredetection.TransactionEdge
import com.signalcollect.fraudppuccino.structuredetection.TransactionLinker
import com.signalcollect.fraudppuccino.structuredetection.SignalBroadcaster
import com.signalcollect.fraudppuccino.structuredetection.TransactionPatternEdge

object Test2ComponentGraph extends App {

  //Vertex algorithms uses in the computation
  val signalMultiplexig: RepeatedAnalysisVertex[_] => VertexAlgorithm = vertex => new SignalBroadcaster(vertex)
  val transactionLinking: RepeatedAnalysisVertex[_] => VertexAlgorithm = vertex => new TransactionLinker(vertex)

  // Step 0: Build the Graph
  val accounts = Map[Int, RepeatedAnalysisVertex[_]]()
  val transactions = Map[Int, RepeatedAnalysisVertex[_]]()

  val graph = GraphBuilder.build

  for (i <- 0 until 9000) {
    val account = new RepeatedAnalysisVertex(i)
    accounts.put(i, account)
    graph.addVertex(account)
  }

  //loadTransactions("datasets/demoGraphInput1.txt")
  loadTransactions("datasets/gnutella.input")

  
  // Step 1: Link Transactions iff they belong together in some way
  accounts.values.foreach(_.setAlgorithmImplementation(signalMultiplexig))
  transactions.values.foreach(_.setAlgorithmImplementation(transactionLinking))
  graph.recalculateScores

  println(graph.execute)
  //transactions.foreach(println(_))

  // Step 2: Find Interlinked Patterns 
  // Maybe one could use the idea of pattern matching here..
  

  println("PatternEdges " + transactions.values.count(_.outgoingEdges.filter(_._2.isInstanceOf[TransactionPatternEdge]).nonEmpty))

  // IO Utility function
  def loadTransactions(filename: String) {
    var count = 100000

    for (line <- Source.fromFile(filename).getLines) {
      val splitted = line.split(" ")
      val sourceAccountId = splitted(0).toInt
      val targetAccountId = splitted(1).toInt
      val value = splitted(2).toInt
      val time = try {
        splitted(3).toInt
      } catch {
        case e: Exception => 0
      }

      val transactionVertex = new RepeatedAnalysisVertex(count)
      transactionVertex.storeAttribute("value", value)
      transactionVertex.storeAttribute("time", time)
      transactionVertex.storeAttribute("src", sourceAccountId)
      transactionVertex.storeAttribute("target", targetAccountId)
      count += 1
      transactions.put(transactionVertex.id, transactionVertex)
      graph.addVertex(transactionVertex)

      graph.addEdge(sourceAccountId, new TransactionEdge(transactionVertex.id))
      graph.addEdge(transactionVertex.id, new TransactionEdge(targetAccountId))
    }
  }
}