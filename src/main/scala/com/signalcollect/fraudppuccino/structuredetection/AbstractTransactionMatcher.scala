package com.signalcollect.fraudppuccino.structuredetection

import com.signalcollect._
import com.signalcollect.fraudppuccino.repeatedanalysis._
import scala.collection.mutable.ArrayBuffer
import java.util.HashMap
import scala.collection.JavaConversions._
import java.util.ArrayList

abstract class AbstractTransactionMatcher(vertex: RepeatedAnalysisVertex[_]) extends VertexAlgorithm(vertex) {

  var matchableInputs = new ArrayBuffer[ArrayBuffer[PartialInput]] // Transactions that are received by this entity
  var matchableOutputs = new ArrayBuffer[ArrayBuffer[PartialOutput]] // Transactions that are sent by this entity
  val matchesFound = ArrayBuffer[(Iterable[TransactionInput], Iterable[TransactionOutput])]()
  val uncollectedOutputs = ArrayBuffer[TransactionOutput]()

  def getState

  def setState(state: Any)

  def deliverSignal(signal: Any, sourceId: Option[Any], graphEditor: GraphEditor[Any, Any]) = {
    signal match {
      case input: TransactionInput =>
        processInputTransaction(input, graphEditor); true
      case output: TransactionOutput => {
        uncollectedOutputs += output
        scoreCollect = 1.0
        false
      }
      case timeoutPill: Array[Long] => {
        matchableInputs = matchableInputs.dropWhile(_.head.earliestTime < timeoutPill(0))
        matchableOutputs = matchableOutputs.dropWhile(_.head.earliestTime < timeoutPill(0))

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
          graphEditor.sendSignal((out.transactionID, DownstreamTransactionPatternEdge), in.transactionID, None)
          graphEditor.sendSignal((in.transactionID, UpstreamTransactionPatternEdge), out.transactionID, None)
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

  def processInputTransaction(input: TransactionInput, graphEditor: GraphEditor[Any, Any]) {
    if (matchableInputs.size < 10) {
      matchableInputs.foreach(inputs => inputs ++= inputs.map(_.extend(input)))
      matchableInputs += ArrayBuffer(PartialInput(Array(input), input.value))
    }
  }

  /**
   * Tries to find matching input and output transactions and then bi-connects them using pattern edges
   */
  def processOutputTransaction(output: TransactionOutput, graphEditor: GraphEditor[Any, Any]) {

    if (matchableOutputs.size < 10) {

      matchableOutputs.foreach(outputs => outputs ++= outputs.map(partialOutput => {
        val newPartialOutput = partialOutput.extend(output)
        matchableInputs.foreach(inputs => inputs.foreach(input => {
          if (input.latestTime < newPartialOutput.earliestTime && Math.abs(input.sum - newPartialOutput.sum).toDouble / newPartialOutput.sum < 0.1) {
            matchesFound += ((input.members, newPartialOutput.members))
            scoreSignal = 1.0
          }
        }))
        newPartialOutput
      }))

      matchableInputs.foreach(inputs => inputs.foreach(input => {
        if (input.latestTime < output.time && Math.abs(input.sum - output.value).toDouble / output.value < 0.1) {
          matchesFound += ((input.members, List(output)))
          scoreSignal = 1.0
        }
      }))
      
      matchableOutputs += ArrayBuffer(PartialOutput(Array(output), output.value))
    }
  }

  //To be defined depending on the actual use case
  def findMatchingTransactions(newOutPut: TransactionOutput, outputs: Iterable[TransactionOutput], inputs: Iterable[TransactionInput]): (Iterable[TransactionInput], Iterable[TransactionOutput])

}