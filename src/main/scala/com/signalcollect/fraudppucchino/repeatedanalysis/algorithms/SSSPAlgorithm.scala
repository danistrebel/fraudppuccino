package com.signalcollect.fraudppucchino.repeatedanalysis.algorithms

import com.signalcollect._
import com.signalcollect.fraudppucchino.repeatedanalysis._

class SSSPAlgorithm(vertex: RepeatedAnalysisVertex[_]) extends VertexAlgorithm {

  var minDistance = Int.MaxValue
  var minSignal = Int.MaxValue

  def getState = minDistance

  def setState(state: Any) = {
    state match {
      case x: Int => minDistance = x
      case _ => throw new Exception("State not compatible")
    }
    scoreSignal = 1.0
  }

  def deliverSignal(signal: Any, sourceId: Option[Any], graphEditor: GraphEditor[Any, Any]) = {
    signal match {
      case x: Int =>
        minSignal = Math.min(minSignal, x); false
      case _ => throw new Exception("State not compatible");
    }
  }

  def executeSignalOperation(graphEditor: GraphEditor[Any, Any], outgoingEdges: Iterable[Edge[_]]) {
    outgoingEdges.foreach(edge => {
     graphEditor.sendSignal(minDistance+1, edge.id.targetId, Some(edge.sourceId))
    })
    scoreSignal = 0.0
  }

  def executeCollectOperation(graphEditor: GraphEditor[Any, Any]) = {
    minDistance = Math.min(minDistance, minSignal)
    scoreSignal = 1.0
  }

  var scoreSignal = 0.0

  def scoreCollect: Double = {
    if (minDistance > minSignal) {
      1.0
    } else {
      0.0
    }
  }

  def noitfyTopologyChange {
    //Not considered
  }

}