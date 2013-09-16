package com.signalcollect.fraudppuccino.detection

import com.signalcollect._
import com.signalcollect.fraudppuccino.repeatedanalysis._

/**
 * Connects transactions that are a logically subsequent of each other
 */ 
abstract class TransactionPatternEdge extends EdgeMarker

case object DownstreamTransactionPatternEdge extends TransactionPatternEdge //Pattern in the direction of the transaction flow
case object UpstreamTransactionPatternEdge extends TransactionPatternEdge // Pattern in the opposite direction of the transaction flow

class DownstreamTransactionEdge(targetId: Int) extends EdgeMarkerWrapper(targetId, DownstreamTransactionPatternEdge)
class UpstreamTransactionEdge(targetId: Int) extends EdgeMarkerWrapper(targetId, UpstreamTransactionPatternEdge)