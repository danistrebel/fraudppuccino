package com.signalcollect.transactionsenders

import com.signalcollect.DataGraphVertex
import com.signalcollect.pd.analysis.VertexAlgorithm
import com.signalcollect._
import java.util.HashSet


class SignalMultiplexer(vertex : Vertex[_,_]) extends VertexAlgorithm {
  
  val signalsReceived = new HashSet[Any]

  def getState = None

  def setState(state: Any) = {
  }

  def deliverSignal(signal: Any, sourceId: Option[Any], graphEditor: GraphEditor[Any, Any]) = {
    signalsReceived.add(signal)
    true
  }

  def executeSignalOperation(graphEditor: GraphEditor[Any, Any], outgoingEdges: Iterable[Edge[_]]) {
    for(edge <- outgoingEdges) {
      for(signal <- signalsReceived.toArray) {
        graphEditor.sendSignal(signal, edge.targetId, Some(edge.id.sourceId))
      }
    }
    signalsReceived.clear
  }

  def executeCollectOperation(graphEditor: GraphEditor[Any, Any]) = {

  }

  def scoreSignal = if(signalsReceived.size> 0) 1.0 else 0.0;

  def scoreCollect: Double = {
    0.0
  }

  def noitfyTopologyChange {
    //Not considered
  }

}