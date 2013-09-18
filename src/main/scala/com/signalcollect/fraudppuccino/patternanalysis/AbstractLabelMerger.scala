package com.signalcollect.fraudppuccino.patternanalysis

import com.signalcollect.fraudppuccino.repeatedanalysis._
import com.signalcollect._

abstract class AbstractLabelMerger[LabelType](vertex: RepeatedAnalysisVertex[_]) extends VertexAlgorithm(vertex) {

  var label = initialLabel
  def getState = label

  def setState(state: Any) = {
  }

  def deliverSignal(signal: Any, sourceId: Option[Any], graphEditor: GraphEditor[Any, Any]) = {
    signal match {
      case newLabel: LabelType => {
        if(shouldSwitchToLabel(newLabel)) {
          label = newLabel
          vertex.outgoingEdges.forall(edge => shouldSignalForEdgeType(edge._2))
        }
      }
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

  def initialLabel: LabelType

  def shouldSwitchToLabel(newLabel: LabelType): Boolean
  
  def shouldSignalForEdgeType(edgeType: EdgeMarker) : Boolean
}