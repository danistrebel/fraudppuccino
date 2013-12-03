package com.signalcollect.fraudppuccino.structuredetection

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.SpecificationWithJUnit
import com.signalcollect._
import com.signalcollect.fraudppuccino.repeatedanalysis._
import scala.collection.mutable.Map
import com.signalcollect.fraudppuccino.structuredetection._
import com.signalcollect.fraudppuccino.querylanguage.StreamingExecutionDemo
import com.signalcollect.fraudppuccino.querylanguage.StreamingExecution
import com.signalcollect.fraudppuccino.repeatedanalysis.EdgeMarkers._

@RunWith(classOf[JUnitRunner])
class StreamedStructureDetectionSpec extends SpecificationWithJUnit {

  "A Streamed Structure Detection" should {
    
    val txAttributes = Map[String, (Int, String => Any)]()
    txAttributes += (("id", (0, v => v.toInt)))
    txAttributes += (("src", (2, v => v.toInt)))
    txAttributes += (("target", (3, v => v.toInt)))
    txAttributes += (("time", (5, v => v.toLong)))
    txAttributes += (("value", (4, v => v.toLong)))





    sequential

    val execution = new StreamingExecution(transactionAttributes = txAttributes.toMap)

    "add Transactions to the transaction matcher " in {

      execution.loadTransaction(Array("0", "0", "100", "101", "100", "0"))
      execution.loadTransaction(Array("2", "0", "100", "102", "300", "0"))
      execution.loadTransaction(Array("3", "0", "104", "105", "200", "0"))
      execution.loadTransaction(Array("1", "0", "101", "102", "100", "1"))
      execution.graph.recalculateScores
      execution.graph.execute

      //Simulate next computation step
      execution.graph.foreachVertexWithGraphEditor(graphEditor => vertex =>
        vertex.deliverSignal(Array[Long](0l, 0l), None, graphEditor))
      execution.graph.recalculateScores
      execution.graph.execute
            
      execution.graph.forVertexWithId(vertexId = 0, f = { v: RepeatedAnalysisVertex[_] => v.outgoingEdges.count(_._2 == DownstreamTransactionPatternEdge) }) === 1
      execution.graph.forVertexWithId(vertexId = -1, f = { v: RepeatedAnalysisVertex[_] => v.outgoingEdges.count(_._2 == UpstreamTransactionPatternEdge) }) === 1
    }

    "add transaction to the chain if within the time window" in {
      execution.retire(Array(0l, 100l))
      execution.loadTransaction(Array("4", "0", "102", "103", "300", "5"))

      execution.graph.recalculateScores
      execution.graph.execute
      
      //Simulate next computation step
      execution.graph.foreachVertexWithGraphEditor(graphEditor => vertex =>
        vertex.deliverSignal(Array[Long](0l, 0l), None, graphEditor))
      execution.graph.recalculateScores
      execution.graph.execute
      
      execution.graph.forVertexWithId(vertexId = -2, f = { v: RepeatedAnalysisVertex[_] => v.outgoingEdges.count(_._2 == DownstreamTransactionPatternEdge) }) === 1
      execution.graph.forVertexWithId(vertexId = -4, f = { v: RepeatedAnalysisVertex[_] => v.outgoingEdges.count(_._2 == UpstreamTransactionPatternEdge) }) === 1
    }

    "NOT add transaction to the chain if they are outside of the time window" in {
      execution.retire(Array(1l, 100l))
      execution.loadTransaction(Array("5", "0", "105", "106", "200", "6"))
      execution.graph.recalculateScores
      execution.graph.execute
      0===0
      
//      //Simulate next computation step
//      execution.graph.foreachVertexWithGraphEditor(graphEditor => vertex =>
//        vertex.deliverSignal(Array[Long](0l, 0l), None, graphEditor))
//      execution.graph.recalculateScores
//      execution.graph.execute
//      
//      execution.graph.forVertexWithId(vertexId = -5, f = { v: RepeatedAnalysisVertex[_] => v.outgoingEdges.count(_._2 == UpstreamTransactionPatternEdge) }) === 0
    }
  }
}