package com.signalcollect.fraudppuccino.componentdetection

import com.signalcollect.fraudppuccino.repeatedanalysis._
import com.signalcollect._


/**
 * Member of a connected component.
 * Serves as a wrapper for yet another VertexAlgorithm and reports it's results back to the master of the connected component.
 */ 
class ComponentMember(vertex: RepeatedAnalysisVertex[_]) extends VertexAlgorithm(vertex) {
  
  //Underlying algorithm implementation
  var embeddedAlgorithm: VertexAlgorithm = new ComponentMemberAnnoncer(vertex)
  
  def getState = None

  def setState(state: Any) = {
  }

  /*
   * Handles all component internal communication e.g. sent by the component master
   * or delegates the signal to the current algorithm implementation
   */ 
  def deliverSignal(signal: Any, sourceId: Option[Any], graphEditor: GraphEditor[Any, Any]) = {
    signal match {
      case ComponentMemberQuery(key) => graphEditor.sendSignal(ComponentMemberResponse(vertex.getResult(key)), sourceId.get, Some(vertex.id))
      case ComponentMemberAlgorithm(algorithmFactory) => embeddedAlgorithm = algorithmFactory(vertex)
      case _ => embeddedAlgorithm.deliverSignal(signal, sourceId, graphEditor)
    }
    true
  }

  //Delegate all algorithm specific methods to the wrapped algorithm
  def executeSignalOperation(graphEditor: GraphEditor[Any, Any], outgoingEdges: Iterable[(Any, EdgeMarker)]) = embeddedAlgorithm.executeSignalOperation(graphEditor, outgoingEdges)
  def executeCollectOperation(graphEditor: GraphEditor[Any, Any]) = embeddedAlgorithm.executeCollectOperation(graphEditor)
  def scoreSignal = embeddedAlgorithm.scoreSignal
  def scoreCollect = embeddedAlgorithm.scoreCollect
  def notifyTopologyChange = embeddedAlgorithm.notifyTopologyChange
}