package com.signalcollect.fraudppuccino.evaluation.btc

import com.signalcollect.fraudppuccino.detection._
import com.signalcollect.fraudppuccino.repeatedanalysis.RepeatedAnalysisVertex

class BTCTransactionMatcher(vertex: RepeatedAnalysisVertex[_]) extends AbstractTransactionMatcher(vertex) {

  var balance: Long = 0l

  def getState = balance

  def setState(state: Any) = {
    state match {
      case balanceState: Long => balance = balanceState
      case _ =>
    }
  }

  def findMatchingTransactions(newOutPut: TransactionOutput,
    outputs: Iterable[TransactionOutput],
    inputs: Iterable[TransactionInput]): (Iterable[TransactionInput], Iterable[TransactionOutput]) = {
    
    //Finds chains and aggregations
    findMatchingsubsetSums(inputs, newOutPut) match {
      case Nil =>
      case matches: List[TransactionInput] => return (matches, List(newOutPut))
    }
    
    //Finds splits
    for (input <- inputs.filter(_.value>newOutPut.value)) {
      findMatchingsubsetSums(List(newOutPut) ++ outputs, input) match {
        case Nil => 
        case matches: List[TransactionOutput] => return (List(input), matches)
      }
    }

    (Nil, Nil)
  }

  /**
   * Uses dynamic programming to find signals that sum up to the value of this transaction
   */
  def findMatchingsubsetSums(candidates: Iterable[TransactionSignal], target: TransactionSignal, tolerance: Double = 0.1f): Iterable[TransactionSignal] = {
    var subsets = candidates.map(elem => (List(elem), elem.value, candidates.dropWhile(_ != elem).drop(1)))
    while (!subsets.isEmpty) { //expanding is stopped if the sum is reached or all possible combinations are expanded
      val result = subsets.find(subset => Math.abs(subset._2 - target.value) < tolerance)
      if (result.isDefined) {
        return result.get._1
      }
      subsets = subsets.filter(partialResult => !partialResult._3.isEmpty && partialResult._2 < target.value) //drop all with no more remaining options
      subsets = subsets.flatMap(partialResult => {
        partialResult._3.map(elementToAdd => {
          (elementToAdd :: partialResult._1, partialResult._2 + elementToAdd.value, partialResult._3.dropWhile(_ != elementToAdd).drop(1))
        })
      })
    }
    List()

  }

}