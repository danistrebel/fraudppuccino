package com.signalcollect.fraudppuccino.detection

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.SpecificationWithJUnit
import com.signalcollect._
import com.signalcollect.fraudppuccino.repeatedanalysis._
import com.signalcollect.fraudppuccino.repeatedanalysis.EdgeMarkers._
import scala.collection.mutable.Map
import com.signalcollect.fraudppuccino.structuredetection._
import com.signalcollect.fraudppuccino.componentanalysis.algorithms.PatternDepthAnalyzer

@RunWith(classOf[JUnitRunner])
class PatternDepthSpec extends SpecificationWithJUnit {
  "Peeling Chains " should {
    
    sequential
    
    val graph = GraphBuilder.build

    val transactionsMap = Map[Int, RepeatedAnalysisVertex[_]]()

    val transactions = List((100, 500l, 0l, 0, 1),
      (101, 450l, 1l, 1, 2),
      (102, 50l, 1l, 1, 3),
      (103, 400l, 2l, 2, 4),
      (104, 50l, 2l, 2, 5),
      (105, 350l, 3l, 4, 6),
      (106, 50l, 3l, 4, 7))

    for (transaction <- transactions) {
      val tx = new RepeatedAnalysisVertex(transaction._1)
      tx.storeAttribute("value", transaction._2)
      tx.storeAttribute("time", transaction._3)
      tx.storeAttribute("src", transaction._4)
      tx.storeAttribute("target", transaction._5)
      graph.addVertex(tx)
      transactionsMap += ((tx.id, tx))
    }

    graph.addEdge(100, EdgeMarkerWrapper(DownstreamTransactionPatternEdge, 101))
    graph.addEdge(100, EdgeMarkerWrapper(DownstreamTransactionPatternEdge, 102))
    graph.addEdge(101, EdgeMarkerWrapper(UpstreamTransactionPatternEdge,100))
    graph.addEdge(102, EdgeMarkerWrapper(UpstreamTransactionPatternEdge,100))

    graph.addEdge(101, EdgeMarkerWrapper(DownstreamTransactionPatternEdge, 103))
    graph.addEdge(101, EdgeMarkerWrapper(DownstreamTransactionPatternEdge, 104))
    graph.addEdge(103, EdgeMarkerWrapper(UpstreamTransactionPatternEdge,101))
    graph.addEdge(104, EdgeMarkerWrapper(UpstreamTransactionPatternEdge,101))

    graph.addEdge(103, EdgeMarkerWrapper(DownstreamTransactionPatternEdge, 105))
    graph.addEdge(103, EdgeMarkerWrapper(DownstreamTransactionPatternEdge, 106))
    graph.addEdge(105, EdgeMarkerWrapper(UpstreamTransactionPatternEdge,103))
    graph.addEdge(106, EdgeMarkerWrapper(UpstreamTransactionPatternEdge,103))

    " be found" in {

      graph.foreachVertex(v => v.asInstanceOf[RepeatedAnalysisVertex[_]].setAlgorithmImplementation(rav => new PatternDepthAnalyzer(rav)))

      graph.recalculateScores
      graph.execute

      transactionsMap.get(100).get.state === 0
      transactionsMap.get(101).get.state === 1
      transactionsMap.get(102).get.state === 1
      transactionsMap.get(103).get.state === 2
      transactionsMap.get(104).get.state === 2
      transactionsMap.get(105).get.state === 3
      transactionsMap.get(106).get.state === 3
    }
  }

}