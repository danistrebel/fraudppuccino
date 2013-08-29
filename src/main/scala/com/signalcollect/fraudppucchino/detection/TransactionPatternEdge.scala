package com.signalcollect.fraudppucchino.detection

import com.signalcollect._

class TransactionPatternEdge(targetTransactionID: Int) extends DefaultEdge(targetTransactionID) {
  def signal {
  }
}