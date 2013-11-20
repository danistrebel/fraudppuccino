package com.signalcollect.fraudppuccino.repeatedanalysis.demo

import com.signalcollect._
import com.signalcollect.fraudppuccino.repeatedanalysis._
import scala.collection.JavaConversions.mapAsScalaMap

class PageRankAlgorithm(vertex: RepeatedAnalysisVertex[_]) extends VertexAlgorithm(vertex) {

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

  def executeSignalOperation(graphEditor: GraphEditor[Any, Any], outgoingEdges: Iterable[(Any, EdgeMarker)]) {
    val signal = state / outgoingEdges.size
    outgoingEdges.foreach(edge => {
      graphEditor.sendSignal(signal, edge._1, Some(vertex.id))
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

  def notifyTopologyChange {}

}