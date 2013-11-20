package com.signalcollect.fraudppuccino.componentanalysis.algorithms

import com.signalcollect.fraudppuccino.repeatedanalysis._
import com.signalcollect._
import scala.collection.mutable.ArrayBuffer
import com.signalcollect.fraudppuccino.structuredetection._

/**
 * Detects if the target vertex of this transaction already occurred in the flow of
 * transactions that lead to this transaction 
 */
class CircleDetection(vertex: RepeatedAnalysisVertex[_]) extends VertexAlgorithm(vertex) with TransactionRelationshipExplorer {

  var foundCircle = false

  val accountTrail = ArrayBuffer[Any]()
  
  def getState = foundCircle

  def setState(state: Any) = {
    state match {
      case circle: Boolean => foundCircle = circle
      case _ =>
    }
  }

  def deliverSignal(signal: Any, sourceId: Option[Any], graphEditor: GraphEditor[Any, Any]) = {
    signal match {
      case signaledTrail: Iterable[Any] => {
    	  if(accountTrail.exists(_==targetId)) {
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
    outgoingEdges.filter(edge => edge._2 == DownstreamTransactionPatternEdge).foreach(edge => graphEditor.sendSignal(sourceId+:accountTrail, edge._1, Some(vertex.id)))
    scoreSignal = 0
  }

  def executeCollectOperation(graphEditor: GraphEditor[Any, Any]) = {
  }

  var scoreSignal = if (isPatternSource) 1.0 else 0.0

  var scoreCollect = 0.0

  def notifyTopologyChange {
  }
}
