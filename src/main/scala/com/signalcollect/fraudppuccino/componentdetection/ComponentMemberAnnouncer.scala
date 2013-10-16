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
    println("sign")
	scoreSignal = 0.0
    graphEditor.sendSignal(ComponentMemberRegistration, vertex.getResult("component").get.asInstanceOf[Int], Some(vertex.id))
  }

  def executeCollectOperation(graphEditor: GraphEditor[Any, Any]) = {  
  }

  var scoreSignal = 1.0

  var scoreCollect = 0.0

  def notifyTopologyChange {
  }
}