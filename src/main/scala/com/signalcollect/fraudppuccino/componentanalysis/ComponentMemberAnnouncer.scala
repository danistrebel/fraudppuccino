package com.signalcollect.fraudppuccino.componentanalysis

import com.signalcollect._
import com.signalcollect.fraudppuccino.repeatedanalysis._
import com.signalcollect.fraudppuccino.structuredetection._
import com.signalcollect.fraudppuccino.repeatedanalysis.EdgeMarkers._

/**
 * Lets the component members report themselves at the component master.
 * Requires that the vertex already learned about the id of its component
 * master and stored it in a "component" field.
 */ 
case class ComponentMemberAnnoncer(vertex : RepeatedAnalysisVertex[_]) extends VertexAlgorithm {
def getState = None

  def setState(state: Any) = {
  }

  def deliverSignal(signal: Any, sourceId: Option[Any], graphEditor: GraphEditor[Any, Any]) = {
   true
  }

  def executeSignalOperation(graphEditor: GraphEditor[Any, Any], outgoingEdges: Iterable[(Any, EdgeMarker)]) {
    val neighbors = vertex.outgoingEdges.filter(edge => edge._2==EdgeMarkers.DownstreamTransactionPatternEdge || edge._2==EdgeMarkers.UpstreamTransactionPatternEdge).map(_._1.asInstanceOf[Int])
    graphEditor.sendSignal(ComponentMemberRegistration(neighbors.toArray), vertex.getResult("component").get.asInstanceOf[Int], Some(vertex.id))
    scoreSignal = 0.0
  }

  def executeCollectOperation(graphEditor: GraphEditor[Any, Any]) = {  
  }

  var scoreSignal = 1.0

  var scoreCollect = 0.0

  def notifyTopologyChange {
  }
}