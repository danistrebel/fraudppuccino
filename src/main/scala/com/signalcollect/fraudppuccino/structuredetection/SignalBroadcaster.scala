package com.signalcollect.fraudppuccino.structuredetection

import com.signalcollect.fraudppuccino.repeatedanalysis._
import com.signalcollect._

/**
 * Simple VertexAlgorithm implementation that takes whatever signal
 * it receives and broadcasts it along all its outgoing edges.
 */
class SignalBroadcaster(vertex: RepeatedAnalysisVertex[_]) extends VertexAlgorithm {

  def getState = None

  def setState(state: Any) = {
  }

  def deliverSignal(signal: Any, sourceId: Option[Any], graphEditor: GraphEditor[Any, Any]) = {
    for (edge <- vertex.outgoingEdges) {
      graphEditor.sendSignal(signal, edge._1, Some(vertex.id))
    }
    true
  }

  def executeSignalOperation(graphEditor: GraphEditor[Any, Any], outgoingEdges: Iterable[(Any, EdgeMarker)]) {
  }

  def executeCollectOperation(graphEditor: GraphEditor[Any, Any]) = {

  }

  def scoreSignal = 0.0;

  def scoreCollect: Double = {
    0.0
  }

  def noitfyTopologyChange {
    //Not considered
  }

}