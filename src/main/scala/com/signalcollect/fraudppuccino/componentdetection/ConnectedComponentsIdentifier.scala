package com.signalcollect.fraudppuccino.componentdetection

import com.signalcollect.fraudppuccino.repeatedanalysis._
import com.signalcollect._
import com.signalcollect.fraudppuccino.repeatedanalysis.EdgeMarkers._

import com.signalcollect.interfaces.EdgeId
import com.signalcollect.fraudppuccino.componentanalysis.ComponentMaster
import com.signalcollect.fraudppuccino.componentanalysis.ComponentMember

/**
 * Runs a specialized for of label propagation
 * to label all connected sub-patterns with the
 * smallest id of its members.
 *
 * In later steps this label allows the user to
 * determine if two components are linked to each
 * other or not.
 */
case class ConnectedComponentsIdentifier(vertex: RepeatedAnalysisVertex[_]) extends AbstractLabelMerger[ComponentLabel](vertex) {

  val ownTimeStamp = vertex.getResult("time").get.asInstanceOf[Long]

  var unconnectedChildren = vertex.outgoingEdges.filter(_._2 == DownstreamTransactionPatternEdge).map(_._1)
  var isTerminated = false

  def initialLabel: ComponentLabel = ComponentLabel(vertex.id.asInstanceOf[Int], vertex.getResult("time").get.asInstanceOf[Long])

  def shouldSwitchToLabel(newLabel: ComponentLabel): Boolean = this.label.time < newLabel.time || (this.label.time == newLabel.time && this.label.masterId < newLabel.masterId) //switch to the id of the newer time stamp

  def shouldSignalForEdgeType(edgeType: EdgeMarker): Boolean = edgeType == DownstreamTransactionPatternEdge || edgeType == UpstreamTransactionPatternEdge

  def handleTimeout(timeout: Array[Long], graphEditor: GraphEditor[Any, Any]) = { //Fix the component when the 2nd timeout is met by the most recent member of the component

    if (timeout(1) > this.label.time) { //an entire component times out if the most recent element timed out.
      vertex.storeAttribute("component", label.masterId)

      //Test if this is the component head or a member
      if (this.label.masterId == vertex.id.asInstanceOf[Int]) {
        vertex.nextAlgorithm = (v: RepeatedAnalysisVertex[_]) => new ComponentMaster(v)
      } else {
        vertex.nextAlgorithm = (v: RepeatedAnalysisVertex[_]) => new ComponentMember(v)
      }
    } 
    // to prevent infinitely large components the component can be cut at a specific point in time.
    else if (!isTerminated && timeout.length == 3 && timeout(2) > ownTimeStamp) {
      graphEditor.sendSignal(CutComponent, this.label.masterId, Some(vertex.id))
    }
  }

  override def deliverSignal(signal: Any, sourceId: Option[Any], graphEditor: GraphEditor[Any, Any]) = {
    signal match {
      case timeout: Array[Long] => handleTimeout(timeout, graphEditor)
      case newLabel: ComponentLabel => {
        if (!isTerminated) {
          unconnectedChildren -= sourceId.get
          if (shouldSwitchToLabel(newLabel)) {
            label = newLabel
            scoreSignal = 1.0
          } else if (newLabel != label) {
            graphEditor.sendSignal(label, sourceId.get, Some(vertex.id))
          }
        }
      }
      case CutComponent => {
        if (!isTerminated) {
          isTerminated = true
          cutOffLeafs(graphEditor)
          vertex.outgoingEdges.foreach(edge => graphEditor.sendSignal(ComponentTerminated, edge._1, Some(vertex.id)))
        }
      }
      case ComponentTerminated => {
        if (!isTerminated) {
          isTerminated = true
          cutOffLeafs(graphEditor)
          vertex.outgoingEdges.foreach(edge => if (edge._1 != sourceId.get) graphEditor.sendSignal(ComponentTerminated, edge._1, Some(vertex.id)))
        }

      }
    }
    true
  }

  def cutOffLeafs(graphEditor: GraphEditor[Any, Any]) = {
    unconnectedChildren.foreach(leaf => {
      graphEditor.removeEdge(EdgeId(leaf, vertex.id))
      vertex.removeEdge(leaf, graphEditor)
    })
  }

}

case object CutComponent
case object ComponentTerminated
case class ComponentLabel(masterId: Int, time: Long)
