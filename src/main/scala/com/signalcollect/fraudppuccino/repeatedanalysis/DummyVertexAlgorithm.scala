package com.signalcollect.fraudppuccino.repeatedanalysis

import com.signalcollect._

class DummyVertexAlgorithm() extends VertexAlgorithm(null) {
 def getState = None

  def setState(state: Any) = {
  }

  def deliverSignal(signal: Any, sourceId: Option[Any], graphEditor: GraphEditor[Any, Any]) = {
   true
  }

  def executeSignalOperation(graphEditor: GraphEditor[Any, Any], outgoingEdges: Iterable[(Any, EdgeMarker)]) {
  }

  def executeCollectOperation(graphEditor: GraphEditor[Any, Any]) = {  
  }

  var scoreSignal = 0.0

  var scoreCollect = 0.0

  def noitfyTopologyChange {
  }
}