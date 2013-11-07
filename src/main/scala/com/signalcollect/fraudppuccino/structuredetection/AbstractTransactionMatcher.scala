package com.signalcollect.fraudppuccino.structuredetection

import com.signalcollect._
import com.signalcollect.fraudppuccino.repeatedanalysis._
import scala.collection.mutable.ArrayBuffer
import java.util.HashMap
import scala.collection.JavaConversions._

abstract class AbstractTransactionMatcher(vertex: RepeatedAnalysisVertex[_]) extends VertexAlgorithm(vertex) {

  var matchableInputs = new ArrayBuffer[TransactionInput]() // Transactions that are received by this entity
  var matchableOutputs = new ArrayBuffer[TransactionOutput]() // Transactions that are sent by this entity
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
        matchableInputs = matchableInputs.dropWhile(_.time < timeoutPill(0))
        matchableOutputs = matchableOutputs.dropWhile(_.time < timeoutPill(0))
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
    scoreCollect = 0.0
  }

  var scoreSignal = 0.0

  var scoreCollect = 0.0

  def notifyTopologyChange {
  }

  def processInputTransaction(input: TransactionInput, graphEditor: GraphEditor[Any, Any]) {
    if (matchableInputs.size < 10) {
      matchableInputs += input
    }
  }

  /**
   * Tries to find matching input and output transactions and then bi-connects them using pattern edges
   */
  def processOutputTransaction(output: TransactionOutput, graphEditor: GraphEditor[Any, Any]) {

    val matchedCombination = findMatchingTransactions(output, matchableOutputs, matchableInputs) //Depends on the use case
    matchedCombination match {
      case (ins, outs) => {
        matchesFound += ((ins, outs))
        scoreSignal = 1.0
      }
      case (Nil, Nil) =>
    }
    if (matchableOutputs.size < 10) {
      matchableOutputs += output //If no match -> add it to the unmatched outputs
    }
  }

  //To be defined depending on the actual use case
  def findMatchingTransactions(newOutPut: TransactionOutput, outputs: Iterable[TransactionOutput], inputs: Iterable[TransactionInput]): (Iterable[TransactionInput], Iterable[TransactionOutput])

}