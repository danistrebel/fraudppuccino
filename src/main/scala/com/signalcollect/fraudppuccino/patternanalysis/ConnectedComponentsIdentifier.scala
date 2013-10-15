package com.signalcollect.fraudppuccino.patternanalysis

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

  def initialLabel: (Int, Long) = (Math.abs(vertex.id.asInstanceOf[Int]), vertex.getResult("time").get.asInstanceOf[Long])

  def shouldSwitchToLabel(newLabel: (Int, Long)): Boolean = this.label._2<newLabel._2 //switch to the id of the newer time stamp

  def shouldSignalForEdgeType(edgeType: EdgeMarker): Boolean = edgeType.isInstanceOf[TransactionPatternEdge]
  
  def handleTimeout(timeout: Array[Long]) = if(timeout(1)>this.label._2) {
    vertex.storeAttribute("component", label)
    println("Found Component: " + label)
  }

}