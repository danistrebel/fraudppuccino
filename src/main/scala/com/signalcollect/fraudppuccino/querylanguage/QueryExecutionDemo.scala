package com.signalcollect.fraudppuccino.querylanguage

import com.signalcollect.fraudppuccino.structuredetection._
import com.signalcollect.fraudppuccino.repeatedanalysis._
import com.signalcollect.fraudppuccino.evaluation.btc._
import com.signalcollect.fraudppuccino.patternanalysis._

object QueryExecutionDemo extends App {

  val transactionMatching: RepeatedAnalysisVertex[_] => VertexAlgorithm = vertex => new BTCTransactionMatcher(vertex)
  val transactionAnnouncing: RepeatedAnalysisVertex[_] => VertexAlgorithm = vertex => new TransactionAnnouncer(vertex)
  val dummyAlgorithm: RepeatedAnalysisVertex[_] => VertexAlgorithm = vertex => new DummyVertexAlgorithm()
  val subgraphIdentification: RepeatedAnalysisVertex[_] => VertexAlgorithm = vertex => new ConnectedComponentsIdentifier(vertex)
  val depthExplorer: RepeatedAnalysisVertex[_] => VertexAlgorithm = vertex => new PatternDepthAnalyzer(vertex)

  val execution = new QueryExecution

  execution.load("/Volumes/Data/BTC_August2013/user-user-tx.csv", 1000000, 1100000)

  execution.label(Some("component"), None, subgraphIdentification, dummyAlgorithm)
  execution.label(Some("depth"), None, depthExplorer, dummyAlgorithm)
  val transactionsByComponentId = execution.transactions.groupBy(_.getResult("component").get.asInstanceOf[Int])

  
  val connectedComponents = transactionsByComponentId.filter(_._2.size > 1)
  println("Transactions: " + execution.transactions.size)
  println("Components: " + connectedComponents.size)
  println("their depths: " + connectedComponents.map(_._2.map(_.getResult("depth").get.asInstanceOf[Int]).max))
  println("depth larger than 10: "  + connectedComponents.map(_._2.map(_.getResult("depth").get.asInstanceOf[Int]).max).filter(_ > 10).size)

  println("Larger than 10: "  + connectedComponents.filter(_._2.size > 10).size)
  println("sizes: "  + connectedComponents.filter(_._2.size > 10).map(_._2.size))

  println("Unconnected Transactions: " + transactionsByComponentId.filter(_._2.size == 1).size)

}