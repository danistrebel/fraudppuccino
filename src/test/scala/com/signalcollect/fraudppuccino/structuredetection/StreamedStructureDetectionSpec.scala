package com.signalcollect.fraudppuccino.structuredetection

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.SpecificationWithJUnit
import com.signalcollect._
import com.signalcollect.fraudppuccino.repeatedanalysis._
import scala.collection.mutable.Map
import com.signalcollect.fraudppuccino.structuredetection._
import com.signalcollect.fraudppuccino.patternanalysis._
import com.signalcollect.fraudppuccino.querylanguage.QueryExecution

@RunWith(classOf[JUnitRunner])
class StreamedStructureDetectionSpec extends SpecificationWithJUnit {

  "A Streamed Structure Detection" should {

    sequential

    val execution = new QueryExecution

    "add Transactions to the transaction matcher " in {
      
      execution.loadTransaction(0, 100l, 0l, 100, 101)
      execution.loadTransaction(2, 300l, 0l, 100, 102)
      execution.loadTransaction(3, 200l, 0l, 104, 105)
      execution.graph.recalculateScores
      execution.graph.execute
      
      execution.loadTransaction(1, 100l, 1l, 101, 102)
      execution.graph.recalculateScores
      execution.graph.execute

      execution.graph.forVertexWithId(vertexId = 0, f = { v: RepeatedAnalysisVertex[_] => v.outgoingEdges.count(_._2 == DownstreamTransactionPatternEdge) }) === 1
      execution.graph.forVertexWithId(vertexId = 1, f = { v: RepeatedAnalysisVertex[_] => v.outgoingEdges.count(_._2 == UpstreamTransactionPatternEdge) }) === 1
    }

    "add transaction to the chain if within the time window" in {
      execution.sendPoisonPillToAllOlderThan(Array(0l, 0l));
      execution.loadTransaction(4, 300l, 5l, 102, 103)
      execution.graph.recalculateScores
      execution.graph.execute
      execution.graph.forVertexWithId(vertexId = 2, f = { v: RepeatedAnalysisVertex[_] => v.outgoingEdges.count(_._2 == DownstreamTransactionPatternEdge) }) === 1
      execution.graph.forVertexWithId(vertexId = 4, f = { v: RepeatedAnalysisVertex[_] => v.outgoingEdges.count(_._2 == UpstreamTransactionPatternEdge) }) === 1
    }

    "NOT add transaction to the chain if they are outside of the time window" in {
      execution.sendPoisonPillToAllOlderThan(Array(1l, 0l));
      execution.loadTransaction(5, 200l, 5l, 105, 106)
      execution.graph.recalculateScores
      execution.graph.execute
      execution.graph.forVertexWithId(vertexId = 5, f = { v: RepeatedAnalysisVertex[_] => v.outgoingEdges.count(_._2 == UpstreamTransactionPatternEdge) }) === 0
    }
  }
}