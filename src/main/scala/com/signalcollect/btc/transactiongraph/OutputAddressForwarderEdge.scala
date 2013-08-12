package com.signalcollect.btc.transactiongraph

import com.signalcollect.DefaultEdge

class OutputAddressForwarderEdge(redemingTransactionHash: String, index: Int) extends DefaultEdge(redemingTransactionHash) {
  
  type Source = Transaction
  
  def signal = source.outputAtIndex(index)

}