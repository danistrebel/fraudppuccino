package com.signalcollect.transactionsenders

import com.signalcollect._

class TransactionPatternEdge(targetTransactionID: Int) extends DefaultEdge(targetTransactionID) {
  def signal {
  }
}