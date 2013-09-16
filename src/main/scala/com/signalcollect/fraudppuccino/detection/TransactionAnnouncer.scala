package com.signalcollect.fraudppuccino.detection

import com.signalcollect.fraudppuccino.repeatedanalysis._
import com.signalcollect._

class TransactionAnnouncer(vertex: RepeatedAnalysisVertex[_]) extends VertexAlgorithm {

  val value = vertex.getResult("value").getOrElse(0l).asInstanceOf[Long]
  val time = vertex.getResult("time").getOrElse(0l).asInstanceOf[Long]
  val source = vertex.getResult("src").getOrElse(0).asInstanceOf[Int]
  val target = vertex.getResult("target").getOrElse(0).asInstanceOf[Int]
  def id = vertex.id.asInstanceOf[Int]

  def getState = None

  def setState(state: Any) = {
  }

  def deliverSignal(signal: Any, sourceId: Option[Any], graphEditor: GraphEditor[Any, Any]) = {
    false
  }

  def executeSignalOperation(graphEditor: GraphEditor[Any, Any], outgoingEdges: Iterable[(Any, EdgeMarker)]) {
    graphEditor.sendSignal(TransactionInput(id, value, time), source, Some(id))
    graphEditor.sendSignal(TransactionOutput(id, value, time), target, Some(id))
    scoreSignal = 0.0
  }

  def executeCollectOperation(graphEditor: GraphEditor[Any, Any]) = {
  }

  var scoreSignal = 1.0

  var scoreCollect = 0.0

  def noitfyTopologyChange {
  }

}