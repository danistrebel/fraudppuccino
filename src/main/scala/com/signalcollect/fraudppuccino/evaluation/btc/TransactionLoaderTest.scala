package com.signalcollect.fraudppuccino.evaluation.btc

import scala.io.Source
import com.signalcollect.GraphBuilder
import com.signalcollect.fraudppuccino.repeatedanalysis._
import com.signalcollect.fraudppuccino.detection._
import scala.util.control.Breaks._
import scala.collection.mutable.HashMap
import scala.collection.mutable.Set
import com.signalcollect.fraudppuccino.patternanalysis.ConnectedComponentsIdentifier
import com.signalcollect.fraudppuccino.structuredetection.TransactionEdge
import com.signalcollect.fraudppuccino.structuredetection.TransactionLinker
import com.signalcollect.fraudppuccino.structuredetection.SignalBroadcaster
import com.signalcollect.fraudppuccino.structuredetection.TransactionPatternEdge

object TransactionLoaderTest extends App {

  val graph = GraphBuilder.build

  val signalMultiplexig: RepeatedAnalysisVertex[_] => VertexAlgorithm = vertex => new SignalBroadcaster(vertex)
  val transactionLinking: RepeatedAnalysisVertex[_] => VertexAlgorithm = vertex => new TransactionLinker(vertex)

  breakable {

    for (line <- Source.fromFile("/Volumes/Data/BTC_August2013/user-user-tx.csv").getLines) {
      val splitted = line.split(",")

      if (splitted(0).toInt >= 1200000) {
        break
      }

      if (splitted(0).toInt >= 1000000 && splitted(2).toInt != splitted(3).toInt) {
        val transaction = new RepeatedAnalysisVertex(splitted(0).toInt * -1)
        val sender = new RepeatedAnalysisVertex(splitted(2).toInt)
        val receiver = new RepeatedAnalysisVertex(splitted(3).toInt)

        transaction.storeAttribute("value", splitted(4).toLong)
        transaction.storeAttribute("time", splitted(5).toLong)
        transaction.storeAttribute("src", splitted(2).toInt)
        transaction.storeAttribute("target", splitted(3).toInt)

        transaction.setAlgorithmImplementation(transactionLinking)
        sender.setAlgorithmImplementation(signalMultiplexig)
        receiver.setAlgorithmImplementation(signalMultiplexig)

        graph.addVertex(transaction)
        graph.addVertex(sender)
        graph.addVertex(receiver)

        graph.addEdge(splitted(0).toInt * -1, new TransactionEdge(splitted(2).toInt))
        graph.addEdge(splitted(3).toInt, new TransactionEdge(splitted(0).toInt * -1))
      }

    }
  }

  val runtime = Runtime.getRuntime
  val mb = 1024 * 1024

  for (i <- 0 until 10) {
    System.gc
    Thread.sleep(500);
  }
  println("Used Memory:" + (runtime.totalMemory() - runtime.freeMemory()) / mb)

  println("start profiler and hit ENTER")
  readLine

  graph.recalculateScores
  println(graph.execute)

  graph.foreachVertex(v => {
    if (v.id.asInstanceOf[Int] < 0) {
      v.asInstanceOf[RepeatedAnalysisVertex[Int]].setAlgorithmImplementation(v => new ConnectedComponentsIdentifier(v))
    } else {
      v.asInstanceOf[RepeatedAnalysisVertex[Int]].removeAlgorithmImplementation
    }
  })

  graph.recalculateScores
  graph.execute

  val components = Set[Int]()
  var count = 0

  graph.foreachVertex(v => v match {
    case rav: RepeatedAnalysisVertex[_] => {
      if (rav.outgoingEdges.exists(_._2.isInstanceOf[TransactionPatternEdge])) {
        components += v.state.asInstanceOf[Int]
        count += 1
      }
    }
  })

  println("Components " + components.toString)
  println("Component count " + components.size)
  println("Members " + count)

  graph.shutdown

}