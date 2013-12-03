package com.signalcollect.fraudppuccino.componentdetection

import com.signalcollect.fraudppuccino.repeatedanalysis._
import com.signalcollect._
import com.signalcollect.fraudppuccino.repeatedanalysis.EdgeMarkers._

/**
 * Generic vertex algorithm that can be used to propagate labels amongst members of
 * connected vertices.
 */
abstract class AbstractLabelMerger[LabelType](vertex: RepeatedAnalysisVertex[_]) extends VertexAlgorithm {

  var label = initialLabel
  def getState = label

  def setState(state: Any) = label = state.asInstanceOf[LabelType]

  def deliverSignal(signal: Any, sourceId: Option[Any], graphEditor: GraphEditor[Any, Any]) = {
    signal match {
      case timeout: Array[Long] => handleTimeout(timeout, graphEditor)
      case newLabel: LabelType => {
        if (shouldSwitchToLabel(newLabel)) {
          label = newLabel
          scoreSignal = 1.0
        } else if (newLabel != label) {
          graphEditor.sendSignal(label, sourceId.get, Some(vertex.id))
        }
      }
    }
    true
  }

  /**
   * Signal along all edges where specified
   */
  def executeSignalOperation(graphEditor: GraphEditor[Any, Any], outgoingEdges: Iterable[(Any, EdgeMarker)]) {
    val edgesToSignal = vertex.outgoingEdges.filter(edge => shouldSignalForEdgeType(edge._2))
    edgesToSignal.foreach(edge => graphEditor.sendSignal(label, edge._1, Some(vertex.id)))
    scoreSignal = 0.0
  }

  def executeCollectOperation(graphEditor: GraphEditor[Any, Any]) = {}

  /**
   * label should be signaled upon initialization
   */ 
  var scoreSignal = 1.0

  var scoreCollect = 0.0

  def notifyTopologyChange {}

  /**
   * Initial label for this vertex.
   * If this vertex in unconnected it will also end up with this label. 
   */ 
  def initialLabel: LabelType

  /**
   * Decides if the received label should be accepted 
   */ 
  def shouldSwitchToLabel(newLabel: LabelType): Boolean

  /**
   * Tells the vertex along which kinds of edges labels and label changes should be signaled.
   */ 
  def shouldSignalForEdgeType(edgeType: EdgeMarker): Boolean

  /**
   * handles the periodic time signals that are sent to nodes in the graph.
   */ 
  def handleTimeout(timeout: Array[Long], graphEditor: GraphEditor[Any, Any])
}