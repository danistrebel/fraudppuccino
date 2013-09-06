package com.signalcollection.fraudppucchino.evaluation.btc

import com.signalcollect.DataGraphVertex

/**
 * Input Address of a Transaction 
 */
class BTCInputAddress(id: Int) extends DataGraphVertex(id, id) {
  
  type Signal = Int
  
  def collect = Math.min(signals.min, state)
  
  override def scoreSignal: Double = {
    lastSignalState match {
      case None => 1
      case Some(oldState) => (state - oldState).abs
    }
  }
}

class BTCTransaction(id: Int) extends DataGraphVertex(id, Int.MaxValue) {
  
  type Signal = Int
  
  def collect = {
    Math.min(signals.min, state)
  }
  
  override def scoreSignal: Double = {
    lastSignalState match {
      case None => 1
      case Some(oldState) => (state - oldState).abs
    }
  }
}