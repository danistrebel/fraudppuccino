package com.signalcollect.fraudppuccino.structuredetection

/**
 * Holds input transactions that can be used for matching output transactions
 */ 
trait IntputMatchingSet {
  def extendMatchingSet(transaction: TransactionInput): Unit
  
  /**
   * Returns inputs that can be summed up to the target transaction
   */ 
  def findMatchFor(targetTransaction: TransactionOutput): Iterable[Iterable[Any]]
}

/**
 * Holds output transactions that can be used for matching input transactions
 */ 
trait OutputMatchingSet {
  def extendMatchingSet(transaction: TransactionOutput): Unit
  
  /**
   * Returns outputs that can be summed up to the target transaction
   */ 
  def findMatchFor(targetTransaction: TransactionInput): Iterable[Iterable[Any]]
}