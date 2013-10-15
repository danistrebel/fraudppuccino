package com.signalcollect.fraudppuccino.componentdetection

import com.signalcollect.fraudppuccino.repeatedanalysis._
import com.signalcollect._

abstract class AbstractLabelMerger[LabelType](vertex: RepeatedAnalysisVertex[_]) extends VertexAlgorithm(vertex) {

  var label = initialLabel
  def getState = label

  def setState(state: Any) = {
  }

  def deliverSignal(signal: Any, sourceId: Option[Any], graphEditor: GraphEditor[Any, Any]) = {
    signal match {
      case timeout: Array[Long] => handleTimeout(timeout)
      case newLabel: LabelType => {
        if (shouldSwitchToLabel(newLabel)) {
          label = newLabel
          scoreSignal = 1.0
        }
      }
    }
    true
  }

  /**
   * Signal along all edges where specified
   */ 
  def executeSignalOperation(graphEditor: GraphEditor[Any, Any], outgoingEdges: Iterable[(Any, EdgeMarker)]) {
    vertex.outgoingEdges.filter(edge => shouldSignalForEdgeType(edge._2)).foreach(edge => graphEditor.sendSignal(label, edge._1, Some(vertex.id)))
    scoreSignal = 0.0
  }

  def executeCollectOperation(graphEditor: GraphEditor[Any, Any]) = {
  }

  var scoreSignal = 1.0

  var scoreCollect = 0.0

  def noitfyTopologyChange {
  }

  def initialLabel: LabelType

  def shouldSwitchToLabel(newLabel: LabelType): Boolean

  def shouldSignalForEdgeType(edgeType: EdgeMarker): Boolean
  
  def handleTimeout(timeout: Array[Long])
}