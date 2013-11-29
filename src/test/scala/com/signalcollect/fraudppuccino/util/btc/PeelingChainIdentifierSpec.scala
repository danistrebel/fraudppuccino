package com.signalcollect.fraudppuccino.util.btc

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.SpecificationWithJUnit
import com.signalcollect.GraphBuilder
import com.signalcollect.fraudppuccino.repeatedanalysis.RepeatedAnalysisVertex
import com.signalcollect.fraudppuccino.structuredetection.DownstreamTransactionPatternEdge
import com.signalcollect.fraudppuccino.structuredetection.DownstreamTransactionEdgeWrapper
import com.signalcollect.fraudppuccino.structuredetection.UpstreamTransactionEdgeWrapper
import scala.collection.mutable.Map
import com.signalcollect.fraudppuccino.componentanalysis.algorithms.PeelingChainIdentifier
import com.signalcollect.fraudppuccino.componentanalysis.algorithms.PeelingChain

@RunWith(classOf[JUnitRunner])
class PeelingChainIdentifierSpec extends SpecificationWithJUnit {

  "Peeling Chains " should {
    " be found" in {
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

      graph.addEdge(100, new DownstreamTransactionEdgeWrapper(101))
      graph.addEdge(100, new DownstreamTransactionEdgeWrapper(102))
      graph.addEdge(101, new UpstreamTransactionEdgeWrapper(100))
      graph.addEdge(102, new UpstreamTransactionEdgeWrapper(100))

      graph.addEdge(101, new DownstreamTransactionEdgeWrapper(103))
      graph.addEdge(101, new DownstreamTransactionEdgeWrapper(104))
      graph.addEdge(103, new UpstreamTransactionEdgeWrapper(101))
      graph.addEdge(104, new UpstreamTransactionEdgeWrapper(101))

      graph.addEdge(103, new DownstreamTransactionEdgeWrapper(105))
      graph.addEdge(103, new DownstreamTransactionEdgeWrapper(106))
      graph.addEdge(105, new UpstreamTransactionEdgeWrapper(103))
      graph.addEdge(106, new UpstreamTransactionEdgeWrapper(103))

      graph.foreachVertex(v => v.asInstanceOf[RepeatedAnalysisVertex[_]].setAlgorithmImplementation(rav => new PeelingChainIdentifier(rav)))

      graph.recalculateScores
      graph.execute

      transactionsMap.get(100).get.outgoingEdges.count(_._2 == PeelingChain) === 2
      transactionsMap.get(101).get.outgoingEdges.count(_._2 == PeelingChain) === 2
      transactionsMap.get(102).get.outgoingEdges.count(_._2 == PeelingChain) === 0
      transactionsMap.get(103).get.outgoingEdges.count(_._2 == PeelingChain) === 2
      transactionsMap.get(104).get.outgoingEdges.count(_._2 == PeelingChain) === 0
      transactionsMap.get(105).get.outgoingEdges.count(_._2 == PeelingChain) === 0
      transactionsMap.get(106).get.outgoingEdges.count(_._2 == PeelingChain) === 0

    }
  }

}