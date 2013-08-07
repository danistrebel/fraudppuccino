package com.signalcollect.btc.transactiongraph

import com.signalcollect.DataGraphVertex

class PublicAddress(address: Any) extends DataGraphVertex(address, None: Option[AddressOwner]) {

  type Signal = AddressOwner

  var redeemedIn = None: Option[Transaction]
  
  def collect: Option[AddressOwner] = {
    None
  }
}