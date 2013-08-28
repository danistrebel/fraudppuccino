package com.signalcollect.transactionsenders

import com.signalcollect.DefaultEdge

class TransactionEdge(targetTransactionID: Int) extends DefaultEdge(targetTransactionID) {
  def signal {}
}