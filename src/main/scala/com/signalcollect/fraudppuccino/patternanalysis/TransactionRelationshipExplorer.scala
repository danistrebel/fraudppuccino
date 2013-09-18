package com.signalcollect.fraudppuccino.patternanalysis

import com.signalcollect.fraudppuccino.repeatedanalysis.VertexAlgorithm
import com.signalcollect.fraudppuccino.repeatedanalysis.RepeatedAnalysisVertex
import com.signalcollect.fraudppuccino.structuredetection.DownstreamTransactionPatternEdge
import com.signalcollect.fraudppuccino.structuredetection.UpstreamTransactionPatternEdge
import com.signalcollect.fraudppuccino.structuredetection.UpstreamTransactionEdge

abstract class TransactionRelationshipExplorer(vertex: RepeatedAnalysisVertex[_]) extends VertexAlgorithm {

  /**
   * Returns true if this vertex is a source of a transaction sub-pattern.
   */
  def isPatternSource: Boolean = !hasPredecessors && hasSuccessors
  
  /**
   * Returns true if this vertex is a sink of a transaction sub-pattern
   */ 
  def isPatternSink: Boolean = hasPredecessors && !hasSuccessors
  
  def isSplitter = vertex.outgoingEdges.count(_._2 == DownstreamTransactionPatternEdge) > 1
  
  def isAggregator = vertex.outgoingEdges.count(_._2 == UpstreamTransactionPatternEdge) > 1

  def hasPredecessors: Boolean = vertex.outgoingEdges.exists(_._2 == UpstreamTransactionPatternEdge)
  
  def hasSuccessors: Boolean = vertex.outgoingEdges.exists(_._2 == DownstreamTransactionPatternEdge)
  
  def countPredecessors: Int = vertex.outgoingEdges.count(_._2 == UpstreamTransactionPatternEdge)
  
  def countSuccessors: Int = vertex.outgoingEdges.count(_._2 == UpstreamTransactionPatternEdge)
  
}