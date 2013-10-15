package com.signalcollect.fraudppuccino.componentdetection

import com.signalcollect._
import com.signalcollect.fraudppuccino.repeatedanalysis._

class ComponentMemberAnnoncer(vertex : RepeatedAnalysisVertex[_]) extends VertexAlgorithm(vertex) {
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

  def notifyTopologyChange {
  }
}