package com.signalcollect.fraudppuccino.evaluation.btc

import com.signalcollect.fraudppuccino.detection._
import com.signalcollect.fraudppuccino.repeatedanalysis.RepeatedAnalysisVertex
import com.signalcollect.fraudppuccino.structuredetection.AbstractTransactionMatcher
import com.signalcollect.fraudppuccino.structuredetection.TransactionSignal
import com.signalcollect.fraudppuccino.structuredetection.TransactionOutput
import com.signalcollect.fraudppuccino.structuredetection.TransactionInput
import scala.collection.GenIterable

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
    findMatchingsubsetSums(inputs.filter(_.time<newOutPut.time), newOutPut) match {
      case Nil =>
      case matches: List[TransactionInput] => return (matches, List(newOutPut))
    }

    //Finds splits
    for (input <- inputs.filter(input => input.time < newOutPut.time && input.value > newOutPut.value)) {
      findMatchingsubsetSums(List(newOutPut) ++ outputs.filter(_.time > input.time), input) match {
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
    var expandedCandidates = candidates.map(elem => (List(elem), elem.value, candidates.dropWhile(_ != elem).drop(1)))

    while (!expandedCandidates.isEmpty && expandedCandidates.head._1.size < 8) { //expanding is stopped if the sum is reached or all possible combinations are expanded
      val result = expandedCandidates.find(subset => Math.abs(subset._2 - target.value) < tolerance)
      if (result.isDefined) {
        return result.get._1
      }
      expandedCandidates = expandedCandidates.filter(partialResult => !partialResult._3.isEmpty && partialResult._2 < target.value) //drop all with no more remaining options
      expandedCandidates = expandedCandidates.flatMap(partialResult => {
        partialResult._3.map(elementToAdd => {
          (elementToAdd :: partialResult._1, partialResult._2 + elementToAdd.value, partialResult._3.dropWhile(_ != elementToAdd).drop(1))
        })
      })
    }
    List()
  }
}