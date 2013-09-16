package com.signalcollect.fraudppuccino.evaluation.btc

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.SpecificationWithJUnit
import com.signalcollect.fraudppuccino.detection._
import com.signalcollect.fraudppuccino.detection.TransactionOutput

@RunWith(classOf[JUnitRunner])
class BTCTransactionMatcherSpec extends SpecificationWithJUnit {
  
  val matcher = new BTCTransactionMatcher(null)
  
  " the btc transaction matcher  " should {
    "detect chains" in {
      val inputCandidates = List(TransactionInput(0, 100, 0l), TransactionInput(1, 500, 1l),TransactionInput(2, 700, 3l))
      val outputCandidates = List(TransactionOutput(3, 200, 8l), TransactionOutput(4, 800, 10l))
      val newOutput = TransactionOutput(5, 500, 11l)
      val (ins, outs) = matcher.findMatchingTransactions(newOutput, outputCandidates, inputCandidates)
      ins.head.transactionID === 1
      ins.tail === Nil
      outs.head.transactionID === 5
      outs.tail === Nil
    }
    
    "detect aggregations" in {
      val inputCandidates = List(TransactionInput(0, 200, 0l), TransactionInput(1, 300, 1l),TransactionInput(2, 700, 3l))
      val outputCandidates = List(TransactionOutput(3, 200, 8l), TransactionOutput(4, 800, 10l))
      val newOutput = TransactionOutput(5, 500, 11l)
      val (ins, outs) = matcher.findMatchingTransactions(newOutput, outputCandidates, inputCandidates)
      ins.head.transactionID === 1
      ins.tail.head.transactionID === 0
      ins.tail.tail === Nil
      outs.head.transactionID === 5
      outs.tail === Nil
    }
    
    "detect splits" in {
      val inputCandidates = List(TransactionInput(0, 100, 0l), TransactionInput(1, 300, 1l),TransactionInput(2, 700, 3l))
      val outputCandidates = List(TransactionOutput(3, 200, 8l), TransactionOutput(4, 800, 10l))
      val newOutput = TransactionOutput(5, 500, 11l)
      val (ins, outs) = matcher.findMatchingTransactions(newOutput, outputCandidates, inputCandidates)
      ins.head.transactionID === 2
      ins.tail === Nil
      outs.head.transactionID === 3
      outs.tail.head.transactionID === 5
      outs.tail.tail === Nil
    }
    
    "not detect anyting if no connection can be found" in {
      val inputCandidates = List(TransactionInput(0, 100, 0l), TransactionInput(1, 200, 1l),TransactionInput(2, 700, 3l))
      val outputCandidates = List(TransactionOutput(3, 500, 8l), TransactionOutput(4, 1500, 10l))
      val newOutput = TransactionOutput(5, 1500, 11l)
      val (ins, outs) = matcher.findMatchingTransactions(newOutput, outputCandidates, inputCandidates)
      ins === Nil
      outs === Nil
    }
  }
  
}