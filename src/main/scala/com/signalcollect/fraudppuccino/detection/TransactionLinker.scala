package com.signalcollect.fraudppuccino.detection

import com.signalcollect._
import com.signalcollect.fraudppuccino.repeatedanalysis._
import scala.collection.mutable.LinkedList
import scala.collection.mutable.ArrayBuffer

/**
 * Algorithm on transaction vertices that connects neighboring related transactions
 */
class TransactionLinker(vertex: RepeatedAnalysisVertex[_]) extends VertexAlgorithm {

  val candidateInputs = new ArrayBuffer[TransactionInput]() // Transactions that could serve as inputs for this transaction
  val candidateOutputs = new ArrayBuffer[TransactionOutput]() // Transactions that could serve as outputs for this transactions

  val value = vertex.getResult("value").getOrElse(0l).asInstanceOf[Long]
  val time = vertex.getResult("time").getOrElse(0l).asInstanceOf[Long]

  def getState = None

  def setState(state: Any) = {
    scoreSignal = 1.0
  }

  /**
   * Append the incoming signals to the respective collection
   */
  def deliverSignal(signal: Any, sourceId: Option[Any], graphEditor: GraphEditor[Any, Any]) = {
    signal match {
      case txOutput: TransactionOutput => {
        if (isIsPossibleOutputByTime(txOutput)) {
          if (value < txOutput.value) {
            graphEditor.sendSignal(inputSignature, txOutput.transactionID, Some(vertex.id))
            true
          } else if (candidateOutputs.size < 7) { //To make sure we don't explode
            candidateOutputs.append(txOutput)
            scoreCollect = 1.0
          }
        }
        false

      }
      case txInput: TransactionInput => {
        if (candidateInputs.size < 7) { //To make sure we don't explode
          candidateInputs.append(txInput)
          scoreCollect = 1.0
        }
        false
      }
      case _ => throw new Exception("Signal type of signal: " + signal + " is not compatible")
    }

  }

  /**
   * Initially signal the transactions signature to its sender
   */
  def executeSignalOperation(graphEditor: GraphEditor[Any, Any], outgoingEdges: Iterable[(Any, EdgeMarker)]) {
    for (edge <- outgoingEdges) {
      graphEditor.sendSignal(outputSignature, edge._1, Some(vertex.id))
    }
    scoreSignal = 0.0
  }

  /**
   * Try to match the incoming signals and compute outputs and/or inputs of this transaction
   */
  def executeCollectOperation(graphEditor: GraphEditor[Any, Any]) = {
    //test for outputs that can be summed up to this transaction (transaction acts as a splitter)
    val matchingOutputs = findMatchingsubsetSums(candidateOutputs)
    for (txSignature <- matchingOutputs) {
      graphEditor.addEdge(vertex.id, EdgeMarkerWrapper(txSignature.transactionID, DownstreamTransactionPatternEdge))
      graphEditor.addEdge(txSignature.transactionID, EdgeMarkerWrapper(vertex.id.asInstanceOf[Int], UpstreamTransactionPatternEdge))
      candidateOutputs -= txSignature.asInstanceOf[TransactionOutput]
    }

    //test for inputs that this transaction can be composed of and link them (this transaction acts as chain element or aggregator)
    val matchingInputs = findMatchingsubsetSums(candidateInputs)
    for (txSignature <- matchingInputs) {
      graphEditor.addEdge(txSignature.transactionID, EdgeMarkerWrapper(vertex.id.asInstanceOf[Int], DownstreamTransactionPatternEdge))
      graphEditor.addEdge(vertex.id, EdgeMarkerWrapper(txSignature.transactionID, UpstreamTransactionPatternEdge))
      candidateInputs -= txSignature.asInstanceOf[TransactionInput]
    }

    scoreCollect = 0.0
  }

  var scoreSignal = 1.0

  var scoreCollect = 0.0

  def noitfyTopologyChange {
    //Not considered
  }

  /**
   * A compact representation of this Transaction.
   */
  lazy val inputSignature: TransactionInput = TransactionInput(vertex.id.asInstanceOf[Int], this.value, this.time)
  lazy val outputSignature: TransactionOutput = TransactionOutput(vertex.id.asInstanceOf[Int], this.value, this.time)

  /**
   * Uses dynamic programming to find signals that sum up to the value of this transaction
   */
  def findMatchingsubsetSums(candidates: Iterable[TransactionSignal], tolerance: Double = 0.1f) = {
    var subsets = candidates.map(elem => (List(elem), elem.value, candidates.dropWhile(_ != elem).drop(1)))
    while (!(subsets.exists(subset => Math.abs(subset._2 - this.value) < tolerance) || subsets.isEmpty)) { //expanding is stopped if the sum is reached or all possible combinations are expanded
      subsets = subsets.filter(partialResult => !partialResult._3.isEmpty && partialResult._2 < this.value) //drop all with no more remaining options
      subsets = subsets.flatMap(partialResult => {
        partialResult._3.map(elementToAdd => {
          (elementToAdd :: partialResult._1, partialResult._2 + elementToAdd.value, partialResult._3.dropWhile(_ != elementToAdd).drop(1))
        })
      })
    }
    if (subsets.isEmpty) {
      List()
    } else {
      subsets.head._1
    }
  }

  /**
   * Combines one or more incoming transactions of a node if they could be the source of this transaction
   */
  def findAllMatchingSignals(candidates: Iterable[TransactionSignal], tolerance: Double = 0.1): List[TransactionSignal] = {
    val subsets = candidates.toSet.subsets
    val matchingSubsets = subsets.toList.map(_.toList).filter(txCombo => sumsUpToThisValue(txCombo, tolerance))
    if (matchingSubsets.isEmpty) {
      List()
    } else {
      matchingSubsets.head
    }
  }

  /**
   * Output have to happen after or immediately with this transaction.
   * Outputs are considered within the window size.
   */
  def isIsPossibleOutputByTime(outputCandidate: TransactionOutput, windowSize: Long = 432000l): Boolean = {
    outputCandidate.time - this.time >= 0 && outputCandidate.time - this.time < windowSize
  }

  /**
   * Matching combinations have to sum up to the value of this transaction
   * This is a form of the subset problem (http://en.wikipedia.org/wiki/Subset_sum_problem) which is NP-complete.
   */
  def sumsUpToThisValue(combination: List[TransactionSignal], tolerance: Double = 0.1): Boolean = {
    matchesThisValue(combination.foldLeft(0.0)(_ + _.value), tolerance)
  }

  /**
   * Is considered a match if differs at max 10% from another incoming transaction
   */
  def matchesThisValue(otherValue: Double, tolerance: Double): Boolean = (Math.abs(otherValue - this.value) / this.value) < tolerance

  /**
   * Logs debug for tx with id
   */
  def logFor(id: Int, msg: String) = if (vertex.id == id) println(msg)

  override def toString: String = {
    "links to " + vertex.outgoingEdges
  }
}