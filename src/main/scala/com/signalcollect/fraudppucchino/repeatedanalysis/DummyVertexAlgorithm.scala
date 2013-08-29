package com.signalcollect.fraudppucchino.repeatedanalysis

import com.signalcollect._

class DummyVertexAlgorithm extends VertexAlgorithm {
 def getState = None

  def setState(state: Any) = {
  }

  def deliverSignal(signal: Any, sourceId: Option[Any], graphEditor: GraphEditor[Any, Any]) = {
   false
  }

  def executeSignalOperation(graphEditor: GraphEditor[Any, Any], outgoingEdges: Iterable[Edge[_]]) {
  }

  def executeCollectOperation(graphEditor: GraphEditor[Any, Any]) = {  
  }

  var scoreSignal = 0.0

  var scoreCollect = 0.0

  def noitfyTopologyChange {
  }
}