package com.signalcollect.fraudppuccino.componentanalysis.algorithms

import com.signalcollect.fraudppuccino.repeatedanalysis._
import com.signalcollect._
import scala.collection.mutable.ArrayBuffer
import com.signalcollect.fraudppuccino.structuredetection._
import scala.collection.mutable.HashSet
import com.signalcollect.fraudppuccino.repeatedanalysis.EdgeMarkers._

/**
 * Detects if the target vertex of this transaction already occurred in the flow of
 * transactions that lead to this transaction 
 */
case class CircleDetection(vertex: RepeatedAnalysisVertex[_]) extends VertexAlgorithm with TransactionRelationshipExplorer {

  var foundCircle = false

  val accountTrail = HashSet[Any]()
  
  def getState = foundCircle

  def setState(state: Any) = {
    state match {
      case circle: Boolean => foundCircle = circle
      case _ =>
    }
  }

  def deliverSignal(signal: Any, sourceId: Option[Any], graphEditor: GraphEditor[Any, Any]) = {
    if(vertex.id.asInstanceOf[Int] /10000 == 1000) {
        println(vertex.id + " " + signal)
      }
    signal match {
      case signaledTrail: List[Any] => {
    	  if(!signaledTrail.isEmpty && signaledTrail.tail.contains(targetId)) {
    	    foundCircle = true
    	  }
    	  accountTrail++=signaledTrail
    	  scoreSignal = 1.0
        }
      case _ =>
    }
    true
  }

  def executeSignalOperation(graphEditor: GraphEditor[Any, Any], outgoingEdges: Iterable[(Any, EdgeMarker)]) {
    outgoingEdges.filter(edge => edge._2 == DownstreamTransactionPatternEdge).foreach(edge => graphEditor.sendSignal(sourceId+:accountTrail.toList, edge._1, Some(vertex.id)))
    scoreSignal = 0
  }

  def executeCollectOperation(graphEditor: GraphEditor[Any, Any]) = {
  }

  var scoreSignal = if (isPatternSource) 1.0 else 0.0

  var scoreCollect = 0.0

  def notifyTopologyChange {
  }
}
