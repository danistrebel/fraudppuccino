package com.signalcollect.fraudppuccino.detection.demo

import com.signalcollect.fraudppuccino.repeatedanalysis._
import com.signalcollect._
import scala.collection.mutable._
import scala.io.Source
import com.signalcollect.fraudppuccino.detection._
import com.signalcollect.fraudppuccino.detection.demo._

object WindowedStructureDetection extends App {

  val signalMultiplexig: RepeatedAnalysisVertex[_] => VertexAlgorithm = vertex => new SignalMultiplexer(vertex)

  // Step 0: Build the Graph
  val accounts = Map[Int, RepeatedAnalysisVertex[_]]()
  val transactions = Map[Int, RepeatedAnalysisVertex[_]]()

  val graph = GraphBuilder.build

  for (i <- 0 until 9000) {
    val account = new RepeatedAnalysisVertex(i)
    account.setAlgorithmImplementation(signalMultiplexig)
    accounts.put(i, account)
    graph.addVertex(account)
  }

  val structureDetection = new StructureDetection(graph)
  for (i <- 0 until 3) {
    println("Started " + i)

    loadTransactions("datasets/gnutella.input", structureDetection)

    println("done loading")

    structureDetection.runStructureDetection

    println("execution done")

    structureDetection.retainSubgraphs

    structureDetection.unloadTimeWindow

    println("unloaded")
  }

  // IO Utility function
  def loadTransactions(filename: String, structureDetection: StructureDetection) {
    var count = 100000

    for (line <- Source.fromFile(filename).getLines) {
      val splitted = line.split(" ")
      val sourceAccountId = splitted(0).toInt
      val targetAccountId = splitted(1).toInt
      val value = splitted(2).toDouble
      val time = try {
        splitted(3).toInt
      } catch {
        case e: Exception => 0
      }
      structureDetection.addTransaction(sourceAccountId, targetAccountId, value, time)
    }

  }
}