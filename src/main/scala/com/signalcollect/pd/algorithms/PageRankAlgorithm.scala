package com.signalcollect.pd.algorithms

import com.signalcollect._
import com.signalcollect.pd.analysis.VertexAlgorithm
import com.signalcollect.pd.analysis.RepeatedAnalysisVertex
import scala.collection.JavaConversions.mapAsScalaMap

class PageRankAlgorithm(vertex: RepeatedAnalysisVertex[_]) extends VertexAlgorithm {

  val dampingFactor = 0.85f
  var state = 1 - dampingFactor
  var lastSignalState = Float.MinValue

  val signals: collection.mutable.Map[Any, Float] = new java.util.HashMap[Any, Float](0)

  def getState = state

  def setState(newState: Any) = {
    newState match {
      case x: Float => state = x
      case _ => throw new Exception("State not compatible")
    }
  }

  def deliverSignal(signal: Any, sourceId: Option[Any], graphEditor: GraphEditor[Any, Any]) = {
    signal match {
      case x: Float =>
        signals.put(sourceId, x); false
      case _ => throw new Exception("State not compatible");
    }
  }

  def executeSignalOperation(graphEditor: GraphEditor[Any, Any], outgoingEdges: Iterable[Edge[_]]) {
    val signal = state / outgoingEdges.size
    outgoingEdges.foreach(edge => {
      graphEditor.sendSignal(signal, edge.id.targetId, Some(edge.sourceId))
    })
    lastSignalState = signal
  }

  def executeCollectOperation(graphEditor: GraphEditor[Any, Any]) = {
    state = 1 - dampingFactor + dampingFactor * signals.values.sum
  }

  def scoreSignal: Double = {
    if (lastSignalState < 0) {
      1.0
    } else {
      Math.abs(state - lastSignalState)
    }
  }

  def scoreCollect: Double = {
    if (!signals.isEmpty) {
      1.0
    } else {
      0.0
    }
  }

  def noitfyTopologyChange {}

}