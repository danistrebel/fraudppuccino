package com.signalcollect.fraudppuccino.structuredetection

import com.signalcollect._
import com.signalcollect.fraudppuccino.repeatedanalysis._

/**
 * Connects transactions that are a logically subsequent of each other
 */ 
abstract class TransactionPatternEdge extends EdgeMarker

/**
 * Connects pattern transactions in the direction of the transaction flow i.e. inputs to outputs
 */ 
case object DownstreamTransactionPatternEdge extends TransactionPatternEdge

/**
 * Connects pattern transactions in the opposite direction of the transaction flow i.e. transactions to inputs
 */ 
case object UpstreamTransactionPatternEdge extends TransactionPatternEdge

/**
 * Wrapper that allows the adding of DownstreamTransactionPatternEdge via graph modifications
 */ 
class DownstreamTransactionEdgeWrapper(targetId: Int) extends EdgeMarkerWrapper(targetId, DownstreamTransactionPatternEdge)

/**
 * Wrapper that allows the adding of UpstreamTransactionPatternEdge via graph modifications
 */ 
class UpstreamTransactionEdgeWrapper(targetId: Int) extends EdgeMarkerWrapper(targetId, UpstreamTransactionPatternEdge)