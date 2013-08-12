package com.signalcollect.pd.analysis

import com.signalcollect._

abstract class VertexAlgorithm {
  
  def getState: Any
  
  def setState(state: Any)

  def deliverSignal(signal: Any, sourceId: Option[Any], graphEditor: GraphEditor[Any, Any]): Boolean
  
  def executeSignalOperation(graphEditor: GraphEditor[Any, Any], outgoingEdges: Iterable[Edge[_]])

  def executeCollectOperation(graphEditor: GraphEditor[Any, Any])

  def scoreSignal: Double

  def scoreCollect: Double
  
  def noitfyTopologyChange  
}