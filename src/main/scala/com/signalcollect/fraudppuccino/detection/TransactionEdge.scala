package com.signalcollect.fraudppuccino.detection

import com.signalcollect.DefaultEdge

class TransactionEdge(targetTransactionID: Int) extends DefaultEdge(targetTransactionID) {
  def signal {}
}