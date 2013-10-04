package com.signalcollect.fraudppuccino.evaluation.btc

import com.signalcollect.fraudppuccino.detection._
import com.signalcollect.fraudppuccino.repeatedanalysis.RepeatedAnalysisVertex
import com.signalcollect.fraudppuccino.structuredetection.AbstractTransactionMatcher
import com.signalcollect.fraudppuccino.structuredetection.TransactionSignal
import com.signalcollect.fraudppuccino.structuredetection.TransactionOutput
import com.signalcollect.fraudppuccino.structuredetection.TransactionInput
import scala.collection.GenIterable

class BTCTransactionMatcher(vertex: RepeatedAnalysisVertex[_], matchingMode: MatchingMode = MATCH_ALL) extends AbstractTransactionMatcher(vertex) {

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

    val tolerance = 0.1f

    if (matchingMode == MATCH_CHAIN) {
      inputs.filter(input => (input.time < newOutPut.time) && Math.abs(input.value - newOutPut.value) < tolerance) match {
        case Nil =>
        case matches: Iterable[TransactionInput] => return (List(matches.head), List(newOutPut))
      }
    }

    if (matchingMode == MATCH_AGGREGATION || matchingMode == MATCH_ALL) {
      //Finds chains and aggregations
      findMatchingsubsetSums(inputs.filter(_.time < newOutPut.time), newOutPut) match {
        case Nil =>
        case matches: Iterable[TransactionInput] => return (matches, List(newOutPut))
      }
    }

    if (matchingMode == MATCH_SPLIT || matchingMode == MATCH_ALL) {
      //Finds splits
      for (input <- inputs.filter(input => input.time < newOutPut.time && input.value > newOutPut.value)) {
        findMatchingsubsetSums(List(newOutPut) ++ outputs.filter(_.time > input.time), input) match {
          case Nil =>
          case matches: Iterable[TransactionOutput] => return (List(input), matches)
        }
      }
    }

    (Nil, Nil)
  }

  /**
   *
   * Uses dynamic programming to find signals that sum up to the value of this transaction
   */
  def findMatchingsubsetSums(candidates: Iterable[TransactionSignal], target: TransactionSignal, tolerance: Double = 0.1f): Iterable[TransactionSignal] = {
    val indexedCandidates = candidates.toIndexedSeq
    var expandedCandidates = indexedCandidates.map(elem => (List(elem), elem.value, indexedCandidates.indexOf(elem) + 1))

    while (!expandedCandidates.isEmpty && expandedCandidates.head._1.size < 8) { //expanding is stopped if the sum is reached or all possible combinations are expanded
      val result = expandedCandidates.find(subset => Math.abs(subset._2 - target.value) < tolerance) //checks if a match is already found
      if (result.isDefined) {
        return result.get._1
      }
      expandedCandidates = expandedCandidates.filter(partialResult => partialResult._3 < indexedCandidates.size && partialResult._2 < target.value) //drop all with no more remaining options
      expandedCandidates = expandedCandidates.flatMap(partialResult => {
        (partialResult._3 until indexedCandidates.size).map(indexToAdd => (indexedCandidates(indexToAdd) :: partialResult._1, partialResult._2 + indexedCandidates(indexToAdd).value, indexToAdd + 1))
      })
    }
    List()
  }

}

abstract class MatchingMode

case object MATCH_ALL extends MatchingMode
case object MATCH_CHAIN extends MatchingMode
case object MATCH_AGGREGATION extends MatchingMode
case object MATCH_SPLIT extends MatchingMode