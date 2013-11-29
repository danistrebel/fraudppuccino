package com.signalcollect.fraudppuccino.util.btc

import org.junit.runner.RunWith
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.runner.JUnitRunner
import com.signalcollect._
import com.signalcollect.fraudppuccino.repeatedanalysis.RepeatedAnalysisVertex
import com.signalcollect.fraudppuccino.repeatedanalysis._
import com.signalcollect.fraudppuccino.componentdetection.ConnectedComponentsIdentifier
import com.signalcollect.fraudppuccino.structuredetection.TransactionAnnouncer
import com.signalcollect.fraudppuccino.structuredetection.BTCTransactionMatcher

@RunWith(classOf[JUnitRunner])
class BTCConnectedComponentsSpec extends SpecificationWithJUnit {

  val transactionMatching: RepeatedAnalysisVertex[_] => VertexAlgorithm = vertex => new BTCTransactionMatcher(vertex)
  val transactionAnnouncing: RepeatedAnalysisVertex[_] => VertexAlgorithm = vertex => new TransactionAnnouncer(vertex)

  sequential

  "Connected Components" should {

    " have the same component labels for chains" in {
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

      val transactionInfos = List((tx0, 250l, 0l, 0, 1),
        (tx1, 250l, 1l, 1, 2),
        (tx2, 100l, 1l, 1, 3))

      for (account <- accounts) {
        account.setAlgorithmImplementation(transactionMatching)
        graph.addVertex(account)
      }

      for (transaction <- transactionInfos) {
        transaction._1.storeAttribute("value", transaction._2)
        transaction._1.storeAttribute("time", transaction._3)
        transaction._1.storeAttribute("src", transaction._4)
        transaction._1.storeAttribute("target", transaction._5)
        transaction._1.setAlgorithmImplementation(transactionAnnouncing)
        graph.addVertex(transaction._1)
        graph.recalculateScores
        graph.execute
      }

      val timeout = Array(2l, 3l)
      graph.sendSignal(timeout, 100, None)
      graph.sendSignal(timeout, 101, None)
      graph.sendSignal(timeout, 102, None)

      graph.recalculateScores
      graph.execute

      graph.forVertexWithId[RepeatedAnalysisVertex[_], Int](100, v => v.state.asInstanceOf[(Int, Long)]._1) === 101
      graph.forVertexWithId[RepeatedAnalysisVertex[_], Int](101, v => v.state.asInstanceOf[(Int, Long)]._1) === 101
    }

    " have the same component label for splits" in {
      // Graph:
      // -----------------------------------
      //
      // a0 - t0 -> a1 - t1 -> a2 //split
      //            a1 - t2 -> a3

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

      val transactionInfos = List((tx0, 250l, 0l, 0, 1),
        (tx1, 150l, 1l, 1, 2),
        (tx2, 100l, 1l, 1, 3))

      for (account <- accounts) {
        account.setAlgorithmImplementation(transactionMatching)
        graph.addVertex(account)
      }

      for (transaction <- transactionInfos) {
        transaction._1.storeAttribute("value", transaction._2)
        transaction._1.storeAttribute("time", transaction._3)
        transaction._1.storeAttribute("src", transaction._4)
        transaction._1.storeAttribute("target", transaction._5)
        transaction._1.setAlgorithmImplementation(transactionAnnouncing)
        graph.addVertex(transaction._1)
        graph.recalculateScores
        graph.execute
      }

      val timeout = Array(2l, 3l)
      graph.sendSignal(timeout, 100, None)
      graph.sendSignal(timeout, 101, None)
      graph.sendSignal(timeout, 102, None)

      graph.recalculateScores
      graph.execute
     

      graph.forVertexWithId[RepeatedAnalysisVertex[_], Int](100, v => v.state.asInstanceOf[(Int, Long)]._1) === 102
      graph.forVertexWithId[RepeatedAnalysisVertex[_], Int](101, v => v.state.asInstanceOf[(Int, Long)]._1) === 102
      graph.forVertexWithId[RepeatedAnalysisVertex[_], Int](102, v => v.state.asInstanceOf[(Int, Long)]._1) === 102

    }
  }
}