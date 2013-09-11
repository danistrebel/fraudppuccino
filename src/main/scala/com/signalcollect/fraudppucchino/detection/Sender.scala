package com.signalcollect.fraudppucchino.detection

import com.signalcollect.DataGraphVertex
import com.signalcollect.fraudppucchino.repeatedanalysis.VertexAlgorithm
import com.signalcollect._
import java.util.HashSet
import com.signalcollect.fraudppucchino.repeatedanalysis.EdgeMarker

/**
 * Simple VertexAlgorithm implementation that takes whatever signal
 * it receives and multiplexes it along all its outgoing edges.
 */ 
class SignalMultiplexer(vertex : Vertex[_,_]) extends VertexAlgorithm {
  
  val signalsReceived = new HashSet[Any]

  def getState = None

  def setState(state: Any) = {
  }

  def deliverSignal(signal: Any, sourceId: Option[Any], graphEditor: GraphEditor[Any, Any]) = {
    signalsReceived.add(signal)
    true
  }

  def executeSignalOperation(graphEditor: GraphEditor[Any, Any], outgoingEdges: Iterable[(Any, EdgeMarker)]) {
    for(edge <- outgoingEdges) {
      for(signal <- signalsReceived.toArray) {
        graphEditor.sendSignal(signal, edge._1, Some(vertex.id))
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