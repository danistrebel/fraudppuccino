package com.signalcollection.fraudppucchino.evaluation.btc

import org.specs2.mutable._
import com.signalcollect.GraphBuilder
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import com.signalcollect.StateForwarderEdge

@RunWith(classOf[JUnitRunner])
class InputAddressMergerSpec extends SpecificationWithJUnit {

  "BTC dataset preprocessing " should {
    "merge input addresses that co-occur as inputs for the same transaction " in {
      val graph = GraphBuilder.build
      val txInputsTx = List((1000, List(0, 1, 2)), (1001, List(3, 4)), (1002, List(5)), (1003, List(6, 4)))

      for (txInTx <- txInputsTx) {
        val tx = new BTCTransaction(txInTx._1)
        graph.addVertex(tx)

        txInTx._2.foreach(inputId => {
          val input = new BTCInputAddress(inputId)
          graph.addVertex(input)
          graph.addEdge(input.id, new StateForwarderEdge(tx.id))
          graph.addEdge(tx.id, new StateForwarderEdge(input.id))
        })
      }

      graph.execute

      graph.forVertexWithId[BTCInputAddress, Int](0, v => v.state) === 0
      graph.forVertexWithId[BTCInputAddress, Int](1, v => v.state) === 0
      graph.forVertexWithId[BTCInputAddress, Int](2, v => v.state) === 0
      graph.forVertexWithId[BTCInputAddress, Int](3, v => v.state) === 3
      graph.forVertexWithId[BTCInputAddress, Int](4, v => v.state) === 3
      graph.forVertexWithId[BTCInputAddress, Int](5, v => v.state) === 5
      graph.forVertexWithId[BTCInputAddress, Int](6, v => v.state) === 3

    }

  }
}