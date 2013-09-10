package com.signalcollect.fraudppucchino.evaluation.btc

import scala.io.Source
import com.signalcollect.GraphBuilder
import com.signalcollect.fraudppucchino.repeatedanalysis._
import com.signalcollect.fraudppucchino.detection._
import scala.util.control.Breaks._

object TransactionLoaderTest extends App {

  val graph = GraphBuilder.build

  val signalMultiplexig: RepeatedAnalysisVertex[_] => VertexAlgorithm = vertex => new SignalMultiplexer(vertex)
  val transactionLinking: RepeatedAnalysisVertex[_] => VertexAlgorithm = vertex => new TransactionLinker(vertex)

  breakable {

    for (line <- Source.fromFile("/Volumes/Data/BTC_August2013/user-user-tx.csv").getLines) {
      val splitted = line.split(",")

      if (splitted(0).toInt >= 350000) {
        break
      }

      if(splitted(2).toInt != splitted(3).toInt) {
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
    	  
    	  graph.addEdge(splitted(2).toInt, new TransactionEdge(splitted(0).toInt * -1))
    	  graph.addEdge(splitted(0).toInt * -1, new TransactionEdge(splitted(3).toInt))
      }

    }
  }

  val runtime = Runtime.getRuntime
  val mb = 1024 * 1024

  for (i <- 0 until 10) {
    System.gc
  }
  println("Used Memory:" + (runtime.totalMemory() - runtime.freeMemory()) / mb)

  println(graph.execute)
  
  graph.shutdown

}