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
class ConnectedComponentsIdentifier(vertex: RepeatedAnalysisVertex[_]) extends AbstractLabelMerger[Int](vertex) {

  def initialLabel: Int = vertex.id.asInstanceOf[Int]

  def shouldSwitchToLabel(newLabel: Int): Boolean = this.label>newLabel

  def shouldSignalForEdgeType(edgeType: EdgeMarker): Boolean = edgeType.isInstanceOf[TransactionPatternEdge]
}