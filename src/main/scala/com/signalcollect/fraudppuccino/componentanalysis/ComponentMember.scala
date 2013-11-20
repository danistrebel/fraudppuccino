package com.signalcollect.fraudppuccino.componentanalysis

import com.signalcollect.fraudppuccino.repeatedanalysis._
import com.signalcollect._
import com.signalcollect.fraudppuccino.componentanalysis.algorithms.TransactionRelationshipExplorer

/**
 * Member of a connected component.
 * Serves as a wrapper for yet another VertexAlgorithm and reports it's results back to the master of the connected component.
 */
class ComponentMember(vertex: RepeatedAnalysisVertex[_]) extends VertexAlgorithm(vertex) with TransactionRelationshipExplorer {

  def id = vertex.id
  def getResult(key: String) = vertex.getResult(key)
  def results = vertex.results
  def outgoingEdges = vertex.outgoingEdges
  
  //Underlying algorithm implementation
  var embeddedAlgorithm: VertexAlgorithm = new ComponentMemberAnnoncer(vertex)

  /*
   * Handles all component internal communication e.g. sent by the component master
   * or delegates the signal to the current algorithm implementation
   */
  def deliverSignal(signal: Any, sourceId: Option[Any], graphEditor: GraphEditor[Any, Any]) = {
    signal match {
      case ComponentMemberQuery(queryFunction) => graphEditor.sendSignal(queryFunction(this), sourceId.get, Some(vertex.id))
      case ComponentMemberAlgorithm(algorithmFactory) => embeddedAlgorithm = algorithmFactory(vertex)
      case ComponentMemberElimination => graphEditor.removeVertex(vertex.id)
      case _ => embeddedAlgorithm.deliverSignal(signal, sourceId, graphEditor)
    }
    true
  }

  //Delegate all algorithm specific methods to the wrapped algorithm
  def getState = embeddedAlgorithm.getState
  def setState(state: Any) = embeddedAlgorithm.setState(state)
  def executeSignalOperation(graphEditor: GraphEditor[Any, Any], outgoingEdges: Iterable[(Any, EdgeMarker)]) = embeddedAlgorithm.executeSignalOperation(graphEditor, outgoingEdges)
  def executeCollectOperation(graphEditor: GraphEditor[Any, Any]) = embeddedAlgorithm.executeCollectOperation(graphEditor)
  def scoreSignal = embeddedAlgorithm.scoreSignal
  def scoreCollect = embeddedAlgorithm.scoreCollect
  def notifyTopologyChange = embeddedAlgorithm.notifyTopologyChange
}