package com.signalcollect.fraudppucchino.detection

import com.signalcollect._

abstract class TransactionPatternEdge(targetTransactionId: Int) extends DefaultEdge(targetTransactionId) {
  def signal {
  }
}

class DownstreamTransactionPatternEdge(targetTransactionId : Int) extends TransactionPatternEdge(targetTransactionId) //Pattern in the direction of the transaction flow
class UpstreamTransactionPatternEdge(targetTransactionId : Int) extends TransactionPatternEdge(targetTransactionId) // Pattern in the opposite direction of the transaction flow