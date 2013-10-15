package com.signalcollect.fraudppuccino.componentdetection

import com.signalcollect.fraudppuccino.repeatedanalysis._
import com.signalcollect._

class ComponentMember(vertex: RepeatedAnalysisVertex[_]) extends VertexAlgorithm(vertex) {
  
  var embeddedAlgorithm: VertexAlgorithm = new DummyVertexAlgorithm
  
  def getState = None

  def setState(state: Any) = {
  }

  def deliverSignal(signal: Any, sourceId: Option[Any], graphEditor: GraphEditor[Any, Any]) = {
    signal match {
      case ComponentMemberQuery(key) => graphEditor.sendSignal(ComponentMemberResponse(vertex.getResult(key)), sourceId.get, Some(vertex.id))
      case _ => embeddedAlgorithm.deliverSignal(signal, sourceId, graphEditor)
    }
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

object ComponentMemberRegistration
case class ComponentMemberQuery(key: String)
case class ComponentMemberResponse(response: Option[Any])