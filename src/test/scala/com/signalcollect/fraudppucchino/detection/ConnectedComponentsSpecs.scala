package com.signalcollect.fraudppucchino.detection

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.SpecificationWithJUnit
import com.signalcollect._
import com.signalcollect.fraudppucchino.repeatedanalysis._

@RunWith(classOf[JUnitRunner])
class ConnectedComponentsSpecs extends SpecificationWithJUnit {

  "Connected Components" should {

    " have the same component labels " in {
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
        account.setAlgorithmImplementation(vertex => new SignalMultiplexer(vertex))
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
      graph.addEdge(a0.id, new TransactionEdge(tx0.id))
      graph.addEdge(tx0.id, new TransactionEdge(a1.id))
      graph.addEdge(a1.id, new TransactionEdge(tx1.id))
      graph.addEdge(tx1.id, new TransactionEdge(a2.id))
      //linking unrelated arm
      graph.addEdge(a1.id, new TransactionEdge(tx2.id))
      graph.addEdge(tx2.id, new TransactionEdge(a3.id))

      graph.recalculateScores
      graph.execute
      
      for(tx <- transactions) {
        tx.setAlgorithmImplementation(vertex => new ConnectedComponentsIdentifier(vertex))
      }
      
      for(account <- accounts) {
        account.removeAlgorithmImplementation
      }
      
      graph.recalculateScores
      graph.execute
            
      tx0.state.asInstanceOf[Int] == 100
      tx1.state.asInstanceOf[Int] == 100
      tx2.state.asInstanceOf[Int] == 102

    }
  }
}