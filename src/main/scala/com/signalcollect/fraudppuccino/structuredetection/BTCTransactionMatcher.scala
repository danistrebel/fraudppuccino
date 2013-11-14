package com.signalcollect.fraudppuccino.structuredetection

import com.signalcollect._
import com.signalcollect.fraudppuccino.repeatedanalysis._
import scala.collection.mutable.ArrayBuffer
import java.util.HashMap
import scala.collection.JavaConversions._
import java.util.ArrayList

case class BTCTransactionMatcher(vertex: RepeatedAnalysisVertex[_], matchingMode: MatchingMode = MATCH_ALL) extends VertexAlgorithm(vertex) {

  var matchableInputs = new ArrayBuffer[PartialInput] // Transactions that are received by this entity
  var matchableOutputs = new ArrayBuffer[PartialOutput] // Transactions that are sent by this entity
  val matchesFound = ArrayBuffer[(Iterable[Int], Iterable[Int])]()
  val uncollectedOutputs = ArrayBuffer[TransactionOutput]()

  def getState = None

  def setState(state: Any) = {}

  def deliverSignal(signal: Any, sourceId: Option[Any], graphEditor: GraphEditor[Any, Any]) = {
    signal match {
      case input: TransactionInput =>
        processInputTransaction(input); true
      case output: TransactionOutput => {
        uncollectedOutputs += output
        scoreCollect = 1.0
        false
      }
      case timeoutPill: Array[Long] => {
        matchableInputs = matchableInputs.dropWhile(_.latestTime < timeoutPill(0))
        matchableOutputs = matchableOutputs.dropWhile(_.earliestTime < timeoutPill(0))

        if (matchableInputs.isEmpty && matchableOutputs.isEmpty) {
          graphEditor.removeVertex(vertex.id)
        }
        true
      }
      case _ => true // signal discarded and does not need to be collected.
    }

  }

  def executeSignalOperation(graphEditor: GraphEditor[Any, Any], outgoingEdges: Iterable[(Any, EdgeMarker)]) {
    for ((ins, outs) <- matchesFound) {
      for (in <- ins) {
        for (out <- outs) {
          graphEditor.sendSignal((out, DownstreamTransactionPatternEdge), in, None)
          graphEditor.sendSignal((in, UpstreamTransactionPatternEdge), out, None)
        }
      }
    }
    matchesFound.clear
    scoreSignal = 0.0
  }

  def executeCollectOperation(graphEditor: GraphEditor[Any, Any]) = {
    for (output <- uncollectedOutputs) {
      processOutputTransaction(output, graphEditor);
    }
    uncollectedOutputs.clear
    scoreCollect = 0.0
  }

  var scoreSignal = 0.0

  var scoreCollect = 0.0

  def notifyTopologyChange {
  }

  def processInputTransaction(input: TransactionInput) {
    if (matchableInputs.size < 10) {
      matchableInputs.foreach(inputBranch => inputBranch.extend(input))
      matchableInputs += PartialInput(Array(input.transactionID), input.value, input.time)
    }
  }

  /**
   * Tries to find matching input and output transactions and then bi-connects them using pattern edges
   */
  def processOutputTransaction(output: TransactionOutput, graphEditor: GraphEditor[Any, Any]) {

    if (matchableOutputs.size < 10) {
      //matches aggregations and chains
      //i.e. is there a subset of inputs that matches the new output
      matchableInputs.foreach(partialInputs => tryPartialInputResult(partialInputs, output))

      //matches splits 
      //i.e. is there an input that matches a subset of outputs where the output subset contains the new output
      matchableOutputs.foreach(partialOutputs => partialOutputs.extend(output, tryPartialOutputResult(matchableInputs, _)))

      //appends the new output
      matchableOutputs += PartialOutput(Array(output.transactionID), output.value, output.time)
    }
  }

  def tryPartialInputResult(partialInputs: PartialInput, output: TransactionOutput) {
    if (output.time > partialInputs.latestTime) {
      val ratioInputsOuputs = (partialInputs.sum - output.value).toDouble / output.value
      if (Math.abs(ratioInputsOuputs) < 0.1) {
        matchesFound += ((partialInputs.members, Array(output.transactionID)))
        scoreSignal = 1.0
      } else if (ratioInputsOuputs < 0.1) {
        partialInputs.extensions.foreach(partial => tryPartialInputResult(partial, output))
      }
    }
  }

  def tryPartialOutputResult(inputs: Iterable[PartialInput], partialOutputs: PartialOutput): Iterable[PartialInput] = {
    //inputs that happened before the outputs and are smaller or equal in their value
    val candidateInputs = inputs.filter(input => ((input.sum - partialOutputs.sum).toDouble / partialOutputs.sum) < 0.1 && partialOutputs.earliestTime > input.latestTime)
    for (input <- candidateInputs) {
      if ((input.sum - partialOutputs.sum).toDouble/partialOutputs.sum > -0.1) {
        matchesFound += ((input.members, partialOutputs.members))
        scoreSignal = 1.0
      }
    }
    candidateInputs
  }

}

abstract class MatchingMode

case object MATCH_ALL extends MatchingMode
case object MATCH_CHAIN extends MatchingMode
case object MATCH_AGGREGATION extends MatchingMode
case object MATCH_SPLIT extends MatchingMode
