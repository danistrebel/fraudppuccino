package com.signalcollect.fraudppuccino.componentdetection

import com.signalcollect.fraudppuccino.repeatedanalysis._
import com.signalcollect._
import com.signalcollect.fraudppuccino.structuredetection.TransactionPatternEdge

/**
 * Runs a specialized for of label propagation
 * to label all connected sub-patterns with the
 * smallest id of its members.
 *
 * In later steps this label allows the user to
 * determine if two components are linked to each
 * other or not.
 */
class ConnectedComponentsIdentifier(vertex: RepeatedAnalysisVertex[_]) extends AbstractLabelMerger[(Int, Long)](vertex) {

  def initialLabel: (Int, Long) = (vertex.id.asInstanceOf[Int], vertex.getResult("time").get.asInstanceOf[Long])

  def shouldSwitchToLabel(newLabel: (Int, Long)): Boolean = this.label._2 < newLabel._2 //switch to the id of the newer time stamp

  def shouldSignalForEdgeType(edgeType: EdgeMarker): Boolean = edgeType.isInstanceOf[TransactionPatternEdge]

  def handleTimeout(timeout: Array[Long]) = { //Fix the component when the 2nd timeout is met by the most recent member of the component

    if (timeout(1) > this.label._2) {
      vertex.storeAttribute("component", label._1)

      //Test if this is the component head or a member
      if (this.label._1 == vertex.id.asInstanceOf[Int]) {
        vertex.nextAlgorithm = (v: RepeatedAnalysisVertex[_]) => new ComponentMaster(v)
      } else {
        vertex.nextAlgorithm = (v: RepeatedAnalysisVertex[_]) => new ComponentMember(v)
      }

    }
  }

}