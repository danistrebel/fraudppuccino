package com.signalcollect.fraudppuccino.structuredetection

import com.signalcollect.fraudppuccino.repeatedanalysis._
import com.signalcollect._
import com.signalcollect.fraudppuccino.componentdetection.ConnectedComponentsIdentifier
import com.signalcollect.fraudppuccino.repeatedanalysis.EdgeMarkers._ 

/**
 * Vertex algorithm to be used on financial transactions financial transactions
 * Requires the fields 'src', 'target', 'time' and 'value' to be set on the vertex. 
 */
case class TransactionAnnouncer(vertex: RepeatedAnalysisVertex[_]) extends VertexAlgorithm {

  val value = vertex.getResult("value").getOrElse(throw new Exception("Value is not set on transaction with id " + vertex.id)).asInstanceOf[Long]
  val time = vertex.getResult("time").getOrElse(throw new Exception("Time is not set on transaction with id " + vertex.id)).asInstanceOf[Long]
  val source = vertex.getResult("src").getOrElse(throw new Exception("Src is not set on transaction with id " + vertex.id)).asInstanceOf[Int]
  val target = vertex.getResult("target").getOrElse(throw new Exception("Target is not set on transaction with id " + vertex.id)).asInstanceOf[Int]
  def id = vertex.id.asInstanceOf[Int]

  var timedout = false

  def getState = timedout

  def setState(state: Any) = {
  }

  def deliverSignal(signal: Any, sourceId: Option[Any], graphEditor: GraphEditor[Any, Any]) = {
    signal match {
      case timeoutPill: Array[Long] => //Timeout pill represents the maximum age that a transaction is allowed to have in order to stay in the graph.
        if (!timedout && (time < timeoutPill(0))) {
          handleTimeout(graphEditor)
        }
      case _ =>
    }
    true
  }

  def executeSignalOperation(graphEditor: GraphEditor[Any, Any], outgoingEdges: Iterable[(Any, EdgeMarker)]) {
	graphEditor.sendSignal(TransactionInput(id, value, time), target, Some(id))
    graphEditor.sendSignal(TransactionOutput(id, value, time), source, Some(id))
    scoreSignal = 0.0
  }

  def executeCollectOperation(graphEditor: GraphEditor[Any, Any]) = {
    //if it is connected it should switch to the component identification algorithm
    if (vertex.outgoingEdges.exists(edge => edge._2 == DownstreamTransactionPatternEdge || edge._2 == UpstreamTransactionPatternEdge)) {
      vertex.nextAlgorithm = ((v: RepeatedAnalysisVertex[_]) => new ConnectedComponentsIdentifier(v))
    } else {
      graphEditor.removeVertex(vertex.id)
    }
    scoreCollect = 0.0
  }

  var scoreSignal = 1.0

  var scoreCollect = 0.0

  def notifyTopologyChange {
  }

  def handleTimeout(graphEditor: GraphEditor[Any, Any]) = {
    timedout = true
    scoreCollect = 1.0
  }

}