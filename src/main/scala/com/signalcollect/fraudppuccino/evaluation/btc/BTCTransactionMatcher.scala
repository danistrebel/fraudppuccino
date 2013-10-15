package com.signalcollect.fraudppuccino.evaluation.btc

import com.signalcollect.fraudppuccino.detection._
import com.signalcollect.fraudppuccino.repeatedanalysis.RepeatedAnalysisVertex
import com.signalcollect.fraudppuccino.structuredetection.AbstractTransactionMatcher
import com.signalcollect.fraudppuccino.structuredetection.TransactionSignal
import com.signalcollect.fraudppuccino.structuredetection.TransactionOutput
import com.signalcollect.fraudppuccino.structuredetection.TransactionInput
import scala.collection.GenIterable
import scala.annotation.tailrec
import scala.collection.mutable.ArrayBuffer

case class BTCTransactionMatcher(vertex: RepeatedAnalysisVertex[_], matchingMode: MatchingMode = MATCH_ALL) extends AbstractTransactionMatcher(vertex) {

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
        findMatchingsubsetSums(List(newOutPut) ++ outputs.filter(_.time > input.time), input, firstCandidateIsMandatory = true) match {
          case Nil =>
          case matches: Iterable[TransactionOutput] => return (List(input), matches)
        }
      }
    }

    (Nil, Nil)
  }

  /**
   * Expand partially expandedCandidates with previously unexpandedCandidates
   * 
   * @param partiallyExpandedCandidates Intermediary results pf the form (List of members already expanded, sum of expanded members, index of next unexpanded candidate)
   * @param allCandidates All candidates that are available for expansion
   */ 
  def expandCandidates(partiallyExpandedCandidates: IndexedSeq[(List[TransactionSignal], Long, Int)], allCandidates: IndexedSeq[TransactionSignal]): IndexedSeq[(List[TransactionSignal], Long, Int)] = {
	val unexpandedCandidatesCount = partiallyExpandedCandidates.size
	val allCandidatesCount =  allCandidates.size
	
    @tailrec
    def expandNextCandidate(candidateIndex: Int, indexToAdd: Int, partialResult: ArrayBuffer[(List[TransactionSignal], Long, Int)] = ArrayBuffer()): IndexedSeq[(List[TransactionSignal], Long, Int)] = {
      if (candidateIndex >= unexpandedCandidatesCount) { // All candidates are expanded
        partialResult
      } else if (indexToAdd < 0) { // Start expansion of a new candidate
        expandNextCandidate(candidateIndex, partiallyExpandedCandidates(candidateIndex)._3, partialResult)
      } else if (indexToAdd < allCandidatesCount) { //Expand the current candidate
        val expandedCandidate = (allCandidates(indexToAdd) :: partiallyExpandedCandidates(candidateIndex)._1, partiallyExpandedCandidates(candidateIndex)._2 + allCandidates(indexToAdd).value, indexToAdd + 1)
        expandNextCandidate(candidateIndex, indexToAdd + 1, partialResult += expandedCandidate)
      } else {
        expandNextCandidate(candidateIndex + 1, -1, partialResult)
      }
    }
    expandNextCandidate(0, -1)
  }

  /**
   *
   * Uses dynamic programming to find signals that sum up to the value of this transaction
   */
  def findMatchingsubsetSums(
    candidates: Iterable[TransactionSignal],
    target: TransactionSignal, 
    tolerance: Double = 0.1f,
    firstCandidateIsMandatory: Boolean = false): Iterable[TransactionSignal] = {
    
    val indexedCandidates = candidates.toIndexedSeq
    
    var expandedCandidates: IndexedSeq[(List[TransactionSignal], Long, Int)] = {
      if (firstCandidateIsMandatory) {
        Array((List(indexedCandidates(0)), indexedCandidates(0).value, 1))
      } else {
        indexedCandidates.map(elem => (List(elem), elem.value, indexedCandidates.indexOf(elem) + 1))
      }
    }

    while (!expandedCandidates.isEmpty && expandedCandidates.head._1.size < 8) { //expanding is stopped if the sum is reached or all possible combinations are expanded
      val result = expandedCandidates.find(subset => Math.abs(subset._2 - target.value) < tolerance) //checks if a match is already found
      if (result.isDefined) {
        return result.get._1
      }
      expandedCandidates = expandedCandidates.filter(partialResult => partialResult._3 < indexedCandidates.size && partialResult._2 < target.value) //drop all with no more remaining options
      expandedCandidates = expandCandidates(expandedCandidates, indexedCandidates)
    }
    List()
  }
}

abstract class MatchingMode

case object MATCH_ALL extends MatchingMode
case object MATCH_CHAIN extends MatchingMode
case object MATCH_AGGREGATION extends MatchingMode
case object MATCH_SPLIT extends MatchingMode