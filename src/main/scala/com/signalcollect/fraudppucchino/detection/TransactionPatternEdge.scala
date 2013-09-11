package com.signalcollect.fraudppucchino.detection

import com.signalcollect._
import com.signalcollect.fraudppucchino.repeatedanalysis.EdgeMarker

abstract class TransactionPatternEdge extends EdgeMarker


case object DownstreamTransactionPatternEdge extends TransactionPatternEdge //Pattern in the direction of the transaction flow
case object UpstreamTransactionPatternEdge extends TransactionPatternEdge // Pattern in the opposite direction of the transaction flow