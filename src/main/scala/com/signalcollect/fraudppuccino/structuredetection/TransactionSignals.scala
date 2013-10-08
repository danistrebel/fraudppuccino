package com.signalcollect.fraudppuccino.structuredetection

trait TransactionSignal{
  val transactionID: Int
  val value: Long
  val time : Long 
}

case class TransactionInput(transactionID: Int, value: Long, time : Long) extends TransactionSignal
case class TransactionOutput(transactionID: Int, value: Long, time : Long) extends TransactionSignal