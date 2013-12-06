package com.signalcollect.fraudppuccino.structuredetection

/**
 * Signature of a transaction that is used for matching financial transactions
 */
trait TransactionSignal{
  val transactionID: Int
  val value: Long
  val time : Long 
}

/**
 * Signature of an incoming financial transaction
 */
case class TransactionInput(transactionID: Int, value: Long, time : Long) extends TransactionSignal

/**
 * Signature of an outgoing financial transaction
 */
case class TransactionOutput(transactionID: Int, value: Long, time : Long) extends TransactionSignal