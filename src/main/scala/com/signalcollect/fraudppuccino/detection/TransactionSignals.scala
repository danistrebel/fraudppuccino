package com.signalcollect.fraudppuccino.detection

trait TransactionSignal{
  val transactionID: Int
  val value: Double
  val time : Long 
}

case class TransactionInput(transactionID: Int, value: Double, time : Long) extends TransactionSignal
case class TransactionOutput(transactionID: Int, value: Double, time : Long) extends TransactionSignal