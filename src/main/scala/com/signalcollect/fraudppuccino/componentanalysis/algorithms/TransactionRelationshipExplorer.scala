package com.signalcollect.fraudppuccino.componentanalysis.algorithms

import com.signalcollect.fraudppuccino.repeatedanalysis.VertexAlgorithm
import com.signalcollect.fraudppuccino.structuredetection._

/**
 * Utility to explore node properties about nodes and their direct environment
 */
trait TransactionRelationshipExplorer extends VertexAlgorithm {

  /**
   * Returns true if this vertex is a source of a transaction sub-pattern.
   */
  def isPatternSource: Boolean = !hasPredecessors && hasSuccessors

  /**
   * Returns true if this vertex is a sink of a transaction sub-pattern
   */
  def isPatternSink: Boolean = hasPredecessors && !hasSuccessors

  def isIsolated: Boolean = !getHostVertex.outgoingEdges.exists(_._2.isInstanceOf[TransactionPatternEdge])

  def isSplitter = getHostVertex.outgoingEdges.count(_._2 == DownstreamTransactionPatternEdge) > 1

  def isAggregator = getHostVertex.outgoingEdges.count(_._2 == UpstreamTransactionPatternEdge) > 1

  def hasPredecessors: Boolean = getHostVertex.outgoingEdges.exists(_._2 == UpstreamTransactionPatternEdge)

  def hasSuccessors: Boolean = getHostVertex.outgoingEdges.exists(_._2 == DownstreamTransactionPatternEdge)

  def countPredecessors: Int = getHostVertex.outgoingEdges.count(_._2 == UpstreamTransactionPatternEdge)

  def countSuccessors: Int = getHostVertex.outgoingEdges.count(_._2 == UpstreamTransactionPatternEdge)

  /**
   * @return the id of the source of this transaction
   */
  def sourceId = getHostVertex.getResult("src").get.asInstanceOf[Int]

  /**
   * @return the id of the target of this transaction
   */
  def targetId = getHostVertex.getResult("target").get.asInstanceOf[Int]

  def componentMasterId = getHostVertex.getResult("compontent").get.asInstanceOf[Int]

  def value = getHostVertex.getResult("value").getOrElse(0l).asInstanceOf[Long]

  def time = getHostVertex.getResult("time").getOrElse(0l).asInstanceOf[Long]

  def isXcountry = getHostVertex.getResult("xCountry").getOrElse(false).asInstanceOf[Boolean] //is this transaction cross country

  def isCash = getHostVertex.getResult("cash").getOrElse(false).asInstanceOf[Boolean] //is this transaction cash based

}