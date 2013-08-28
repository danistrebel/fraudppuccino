package com.signalcollect.pd.expander

import com.signalcollect._
import com.signalcollect.pd.analysis._
import scala.io.Source
import scala.collection.mutable.Map

object RunExpander extends App {

  var flowExploration: RepeatedAnalysisVertex[_] => VertexAlgorithm = vertex => new FlowExploration(vertex)
  val nodes = Map[Int, Vertex[_, _]]()

  val graph = GraphBuilder.build

  for (i <- 0 until 12) {
    val account = new RepeatedAnalysisVertex(i)
    account.setAlgorithmImplementation(flowExploration)
    nodes.put(i, account)
    graph.addVertex(account)
  }

  loadTransactions("datasets/demoGraphInput1.txt")
  
  println(graph.execute)
  
  graph.foreachVertex(println(_))

  
  def loadTransactions(filename: String) {
    for (line <- Source.fromFile(filename).getLines) {
      val splitted = line.split(" ")

      val sourceAccount = nodes.get(splitted(0).toInt).get
      val targetAccountId = splitted(1).toInt
      val amount = splitted(2).toInt
      val time = splitted(3).toLong

      
      sourceAccount.addEdge(new Transaction(targetAccountId, amount, time), graph)
      //      sourceAccount.executeSignalOperation(graph)

      //      graph.awaitIdle

    }
  }
}