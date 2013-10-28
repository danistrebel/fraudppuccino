package com.signalcollect.fraudppuccino.structuredetection

import com.signalcollect.fraudppuccino.repeatedanalysis.RepeatedAnalysisVertex
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

  /**
   * Finds connected components where the new output is part of either a chain, or a split or acts as an aggregation.
   * Depending on the MatchingMode of the Matcher all or only a subset of the structures are tested.
   */
  def findMatchingTransactions(newOutPut: TransactionOutput,
    outputs: Iterable[TransactionOutput],
    inputs: Iterable[TransactionInput]): (Iterable[TransactionInput], Iterable[TransactionOutput]) = {

    val tolerance = 0.1f //percentage within which a target is allowed to vary from the sum of the components

    if (matchingMode == MATCH_CHAIN) {
      inputs.filter(input => (input.time < newOutPut.time) && Math.abs(input.value - newOutPut.value) < tolerance) match {
        case Nil =>
        case matches: Iterable[TransactionInput] => return (List(matches.head), List(newOutPut))
      }
    }

    //Finds chains and aggregations
    if (matchingMode == MATCH_AGGREGATION || matchingMode == MATCH_ALL) {
      findAggregations(inputs.filter(_.time < newOutPut.time), newOutPut) match {
        case Nil =>
        case matches: Iterable[TransactionSignal] => return (matches, List(newOutPut))
      }
    }

    //Finds splits where an input is split among the new output and possibly other outputs
    if (matchingMode == MATCH_SPLIT || matchingMode == MATCH_ALL) {
      val inputCandidates = inputs.filter(input => input.time < newOutPut.time && input.value > newOutPut.value) //possible inputs that could be split to the new output
      if (inputCandidates.size > 0) {
        val outputCandidates = outputs.filter(_.time > inputCandidates.map(_.time).min)
        if (outputCandidates.size > 0) { //otherwise it is a chain and would have been detected above
          findSplits(List(newOutPut) ++ outputCandidates, inputCandidates) match {
            case (Nil, Nil) =>
            case matches: (Iterable[TransactionInput], Iterable[TransactionOutput]) => return matches
          }

        }
      }

    }

    (Nil, Nil)
  }

  /**
   * Expand partially expandedCandidates with previously unexpandedCandidates
   *
   * @param partiallyExpandedCandidates Intermediary results of the form (List of members already expanded, sum of expanded members, index of next unexpanded candidate)
   * @param allCandidates All candidates that are available for expansion
   */
  def expandCandidates(partiallyExpandedCandidates: IndexedSeq[(List[TransactionSignal], Long, Int)], allCandidates: IndexedSeq[TransactionSignal]): IndexedSeq[(List[TransactionSignal], Long, Int)] = {
    val unexpandedCandidatesCount = partiallyExpandedCandidates.size
    val allCandidatesCount = allCandidates.size

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
  def findAggregations(
    candidates: Iterable[TransactionInput],
    target: TransactionOutput,
    tolerance: Double = 0.1f): Iterable[TransactionInput] = {

    val indexedCandidates = candidates.toIndexedSeq

    var expandedCandidates: IndexedSeq[(List[TransactionSignal], Long, Int)] =
      indexedCandidates.map(elem => (List(elem), elem.value, indexedCandidates.indexOf(elem) + 1))

    while (!expandedCandidates.isEmpty && expandedCandidates.head._1.size < 8) { //expanding is stopped if the sum is reached or all possible combinations are expanded
      val result = expandedCandidates.find(subset => Math.abs(subset._2 - target.value).toDouble / target.value < tolerance) //checks if a match is already found
      if (result.isDefined) {
        return result.get._1.asInstanceOf[Iterable[TransactionInput]]
      }
      expandedCandidates = expandedCandidates.filter(partialResult => partialResult._3 < indexedCandidates.size && partialResult._2 < target.value) //drop all with no more remaining options
      expandedCandidates = expandCandidates(expandedCandidates, indexedCandidates)
    }
    List()
  }

  def findSplits(
    candidates: Iterable[TransactionOutput],
    targets: Iterable[TransactionInput],
    tolerance: Double = 0.1f): (Iterable[TransactionInput], Iterable[TransactionOutput]) = {

    val indexedCandidates = candidates.toIndexedSeq

    var expandedCandidates: IndexedSeq[(List[TransactionSignal], Long, Int)] =
      Array((List(indexedCandidates(0)), indexedCandidates(0).value, 1))

    while (!expandedCandidates.isEmpty && expandedCandidates.head._1.size < 8) { //expanding is stopped if the sum is reached or all possible combinations are expanded

      targets.foreach(target => {
        val result = expandedCandidates.find(subset =>
          subset._1.map(_.time).min > target.time && Math.abs(subset._2 - target.value).toDouble / target.value < tolerance) //checks if a match is already found
        if (result.isDefined) {
          return (List(target), result.get._1.asInstanceOf[Iterable[TransactionOutput]])
        }
      })

      expandedCandidates = expandedCandidates.filter(partialResult => partialResult._3 < indexedCandidates.size && partialResult._2 < targets.map(_.value).max) //drop all with no more remaining options
      expandedCandidates = expandCandidates(expandedCandidates, indexedCandidates)
    }

    (Nil, Nil)
  }
}

abstract class MatchingMode

case object MATCH_ALL extends MatchingMode
case object MATCH_CHAIN extends MatchingMode
case object MATCH_AGGREGATION extends MatchingMode
case object MATCH_SPLIT extends MatchingMode