package com.signalcollect.btc.transactiongraph

import com.signalcollect.Vertex
import com.signalcollect.DataGraphVertex
import com.signalcollect.DataFlowVertex

class Transaction(hash: String, outputs: List[String] = List[String]()) extends DataGraphVertex(hash, List[String]()) {

  type Signal = String

  override def toString(): String = {
    "transaction: " + hash + " in: " + state + " out: " + outputs
  }

  var redeemedIn = None: Option[Transaction]

  def collect: List[String] = {
    state ++ signals
  }

  def outputAtIndex(index: Int): String = {
    try {
      outputs(index)
    } catch {
      case _ : Throwable=> "unknown"
    }

  }
}