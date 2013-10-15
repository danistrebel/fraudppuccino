package com.signalcollect.fraudppuccino.repeatedanalysis

import com.signalcollect._

/**
 * Defines the structure of an interchangeable algorithm implementation
 */
abstract class VertexAlgorithm(vertex: RepeatedAnalysisVertex[_]) {

  def getState: Any

  def setState(state: Any)

  def deliverSignal(signal: Any, sourceId: Option[Any], graphEditor: GraphEditor[Any, Any]): Boolean

  def executeSignalOperation(graphEditor: GraphEditor[Any, Any], outgoingEdges: Iterable[(Any, EdgeMarker)])

  def executeCollectOperation(graphEditor: GraphEditor[Any, Any])

  def scoreSignal: Double

  def scoreCollect: Double

  def notifyTopologyChange
  
  def getHostVertex = vertex
}