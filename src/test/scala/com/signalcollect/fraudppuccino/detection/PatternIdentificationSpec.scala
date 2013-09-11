package com.signalcollect.fraudppuccino.detection

import org.specs2.mutable._
import com.signalcollect.GraphBuilder
import com.signalcollect.fraudppuccino.repeatedanalysis.RepeatedAnalysisVertex
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PatternIdentificationSpec extends SpecificationWithJUnit {

  "The pattern identifier" should {

    " detect simple transaction chains" in {

      // Graph:
      // -----------------------------------
      //
      // a0 - t0 -> a1 - t1 -> a2 //chain
      //            a1 - t2 -> a3 //unrelated arm

      val graph = GraphBuilder.build

      val a0 = new RepeatedAnalysisVertex(0)
      val a1 = new RepeatedAnalysisVertex(1)
      val a2 = new RepeatedAnalysisVertex(2)
      val a3 = new RepeatedAnalysisVertex(3)

      val accounts = List(a0, a1, a2, a3)

      val tx0 = new RepeatedAnalysisVertex(100)
      val tx1 = new RepeatedAnalysisVertex(101)
      val tx2 = new RepeatedAnalysisVertex(102)

      val transactions = List(tx0, tx1, tx2)

      val transactionInfos = List((tx0, 250l, 0l, 0, 1l),
        (tx1, 250l, 1l, 1, 2l),
        (tx2, 100l, 1l, 1, 3))

      for (account <- accounts) {
        account.setAlgorithmImplementation(vertex => new SignalBroadcaster(vertex))
        graph.addVertex(account)
      }

      for (transaction <- transactionInfos) {
        transaction._1.storeAttribute("value", transaction._2)
        transaction._1.storeAttribute("time", transaction._3)
        transaction._1.storeAttribute("src", transaction._4)
        transaction._1.storeAttribute("target", transaction._5)
        transaction._1.setAlgorithmImplementation(vertex => new TransactionLinker(vertex))
        graph.addVertex(transaction._1)
      }

      //Linking the chain
      graph.addEdge(tx0.id, new TransactionEdge(a0.id))
      graph.addEdge(a1.id, new TransactionEdge(tx0.id))
      graph.addEdge(tx1.id, new TransactionEdge(a1.id))
      graph.addEdge(a2.id, new TransactionEdge(tx1.id))
      //linking unrelated arm
      graph.addEdge(tx2.id, new TransactionEdge(a1.id))
      graph.addEdge(a3.id, new TransactionEdge(tx2.id))

      graph.recalculateScores
      graph.execute

      assert(tx0.outgoingEdges.exists(_._2==DownstreamTransactionPatternEdge))
      assert(!tx1.outgoingEdges.exists(_._2==DownstreamTransactionPatternEdge))
      assert(!tx2.outgoingEdges.exists(_._2==DownstreamTransactionPatternEdge))

      tx0.outgoingEdges.map(_._2).size === 2
    }

    " detect aggregations of patterns" in {

      // Graph:
      // -----------------------------------
      //
      // a0 - t0 -> a1
      //                - t1 -> a2 // aggregation of t0&2
      // a3 - t2 -> a1
      // a4 - t3 -> a1

      val graph = GraphBuilder.build

      val a0 = new RepeatedAnalysisVertex(0)
      val a1 = new RepeatedAnalysisVertex(1)
      val a2 = new RepeatedAnalysisVertex(2)
      val a3 = new RepeatedAnalysisVertex(3)

      val accounts = List(a0, a1, a2, a3)

      val tx0 = new RepeatedAnalysisVertex(100)
      val tx1 = new RepeatedAnalysisVertex(101)
      val tx2 = new RepeatedAnalysisVertex(102)
      val tx3 = new RepeatedAnalysisVertex(103)

      val transactions = List(tx0, tx1, tx2, tx3)

      val transactionInfos = List((tx0, 50l, 0l, 0, 1),
        (tx1, 250l, 2l, 1, 2),
        (tx2, 200l, 1l, 3, 1),
        (tx3, 100l, 1l, 4, 1))

      for (account <- accounts) {
        account.setAlgorithmImplementation(vertex => new SignalBroadcaster(vertex))
        graph.addVertex(account)
      }

      for (transaction <- transactionInfos) {
        transaction._1.storeAttribute("value", transaction._2)
        transaction._1.storeAttribute("time", transaction._3)
        transaction._1.storeAttribute("src", transaction._4)
        transaction._1.storeAttribute("target", transaction._5)
        transaction._1.setAlgorithmImplementation(vertex => new TransactionLinker(vertex))
        graph.addVertex(transaction._1)
      }

      //Linking the aggregator
      graph.addEdge(tx0.id, new TransactionEdge(a0.id))
      graph.addEdge(a1.id, new TransactionEdge(tx0.id))
      graph.addEdge(tx1.id, new TransactionEdge(a1.id))
      graph.addEdge(a2.id, new TransactionEdge(tx1.id))
      graph.addEdge(tx2.id, new TransactionEdge(a3.id))
      graph.addEdge(a1.id, new TransactionEdge(tx2.id))
      //linking unrelated arm
      graph.addEdge(tx3.id, new TransactionEdge(a1.id))
      graph.addEdge(a3.id, new TransactionEdge(tx3.id))

      graph.recalculateScores
      graph.execute

      assert(tx0.outgoingEdges.exists(_._2==DownstreamTransactionPatternEdge))
      assert(!tx1.outgoingEdges.exists(_._2==DownstreamTransactionPatternEdge))
      assert(tx2.outgoingEdges.exists(_._2==DownstreamTransactionPatternEdge))

      tx0.outgoingEdges.map(_._2).size === 2
      tx2.outgoingEdges.map(_._2).size === 2

    }

    "detect splitters of transactions " in {
      // Graph:
      // -----------------------------------
      //
      //               - t1 -> a2
      // a0 - t0 -> a1 //splitted
      // a4 - t3 -> a1 //unrelated
      //               - t2 -> a3
      val graph = GraphBuilder.build

      val a0 = new RepeatedAnalysisVertex(0)
      val a1 = new RepeatedAnalysisVertex(1)
      val a2 = new RepeatedAnalysisVertex(2)
      val a3 = new RepeatedAnalysisVertex(3)
      val a4 = new RepeatedAnalysisVertex(4)

      val accounts = List(a0, a1, a2, a3, a4)

      val tx0 = new RepeatedAnalysisVertex(100)
      val tx1 = new RepeatedAnalysisVertex(101)
      val tx2 = new RepeatedAnalysisVertex(102)
      val tx3 = new RepeatedAnalysisVertex(103)

      val transactions = List(tx0, tx1, tx2, tx3)

      val transactionInfos = List((tx0, 200l, 0l, 0, 1),
        (tx1, 150l, 2l, 1, 2),
        (tx2, 50l, 3l, 1, 3),
        (tx3, 100l, 1l, 4, 1))

      for (account <- accounts) {
        account.setAlgorithmImplementation(vertex => new SignalBroadcaster(vertex))
        graph.addVertex(account)
      }

      for (transaction <- transactionInfos) {
        transaction._1.storeAttribute("value", transaction._2)
        transaction._1.storeAttribute("time", transaction._3)
        transaction._1.storeAttribute("src", transaction._4)
        transaction._1.storeAttribute("target", transaction._5)
        transaction._1.setAlgorithmImplementation(vertex => new TransactionLinker(vertex))
        graph.addVertex(transaction._1)
      }

      //Linking the split
      graph.addEdge(tx0.id, new TransactionEdge(a0.id))
      graph.addEdge(a1.id, new TransactionEdge(tx0.id))
      graph.addEdge(tx1.id, new TransactionEdge(a1.id))
      graph.addEdge(a2.id, new TransactionEdge(tx1.id))
      graph.addEdge(tx2.id, new TransactionEdge(a1.id))
      graph.addEdge(a3.id, new TransactionEdge(tx2.id))
      //linking unrelated input
      graph.addEdge(tx3.id, new TransactionEdge(a4.id))
      graph.addEdge(a1.id, new TransactionEdge(tx3.id))

      graph.recalculateScores
      graph.execute

      tx0.outgoingEdges.filter(_._2==DownstreamTransactionPatternEdge).size === 2
      tx1.outgoingEdges.filter(_._2==DownstreamTransactionPatternEdge).size === 0
      tx2.outgoingEdges.filter(_._2==DownstreamTransactionPatternEdge).size === 0
      tx3.outgoingEdges.filter(_._2==DownstreamTransactionPatternEdge).size === 0

    }
  }

}