package com.signalcollect.fraudppuccino.structuredetection

import com.signalcollect.fraudppuccino.repeatedanalysis._
import com.signalcollect._

case class TransactionAnnouncer(vertex: RepeatedAnalysisVertex[_]) extends VertexAlgorithm(vertex) {

  val value = vertex.getResult("value").getOrElse(0l).asInstanceOf[Long]
  val time = vertex.getResult("time").getOrElse(0l).asInstanceOf[Long]
  val source = vertex.getResult("src").getOrElse(0).asInstanceOf[Int]
  val target = vertex.getResult("target").getOrElse(0).asInstanceOf[Int]
  def id = vertex.id.asInstanceOf[Int]

  var timedout = false

  def getState = timedout

  def setState(state: Any) = {
  }

  def deliverSignal(signal: Any, sourceId: Option[Any], graphEditor: GraphEditor[Any, Any]) = {
    signal match {
      case (linkTarget: Int, edgeType: EdgeMarker) => vertex.outgoingEdges += ((linkTarget, edgeType))
      case timeoutPill: Long => //Timeout pill represents the maximum age that a transaction is allowed to have in order to stay in the graph.
        if (!timedout && time < timeoutPill) {
          graphEditor.sendSignal(TransactionTimedOut, source, Some(id))
          graphEditor.sendSignal(TransactionTimedOut, target, Some(id))
          timedout = true
        }
      case _ =>
    }
    true
  }

  def executeSignalOperation(graphEditor: GraphEditor[Any, Any], outgoingEdges: Iterable[(Any, EdgeMarker)]) {

    graphEditor.sendSignal(TransactionOutput(id, value, time), source, Some(id))
    graphEditor.sendSignal(TransactionInput(id, value, time), target, Some(id))
    scoreSignal = 0.0
  }

  def executeCollectOperation(graphEditor: GraphEditor[Any, Any]) = {}

  var scoreSignal = 1.0

  var scoreCollect = 0.0

  def noitfyTopologyChange {
  }

}