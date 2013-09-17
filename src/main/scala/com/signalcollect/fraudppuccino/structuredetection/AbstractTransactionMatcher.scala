package com.signalcollect.fraudppuccino.structuredetection

import com.signalcollect._
import com.signalcollect.fraudppuccino.repeatedanalysis._
import scala.collection.mutable.ArrayBuffer

abstract class AbstractTransactionMatcher(vertex: RepeatedAnalysisVertex[_]) extends VertexAlgorithm {

  val unmatchedInputs = new ArrayBuffer[TransactionInput]() // Transactions that could serve as inputs for this transaction
  val unmatchedOutputs = new ArrayBuffer[TransactionOutput]() // Transactions that could serve as outputs for this transactions

  def getState

  def setState(state: Any)

  def deliverSignal(signal: Any, sourceId: Option[Any], graphEditor: GraphEditor[Any, Any]) = {
    signal match {
      case input: TransactionInput => processInputTransaction(input, graphEditor)
      case output: TransactionOutput => processOutputTransaction(output, graphEditor)
      case _ => throw new Exception("Unknown signal received: " + signal)
    }
    true
  }

  def executeSignalOperation(graphEditor: GraphEditor[Any, Any], outgoingEdges: Iterable[(Any, EdgeMarker)]) {
  }

  def executeCollectOperation(graphEditor: GraphEditor[Any, Any]) = {
  }

  var scoreSignal = 0.0

  var scoreCollect = 0.0

  def noitfyTopologyChange {
  }

  def processInputTransaction(input: TransactionInput, graphEditor: GraphEditor[Any, Any]) {
    if (unmatchedInputs.size <= 7) {
      unmatchedInputs += input

    }
  }

  /**
   * Tries to find matching input and output transactions and then bi-connects them using pattern edges
   */
  def processOutputTransaction(output: TransactionOutput, graphEditor: GraphEditor[Any, Any]) {
    val matchedCombination = findMatchingTransactions(output, unmatchedOutputs, unmatchedInputs) //Depends on the use case
    matchedCombination match {
      case (Nil, Nil) => if(unmatchedOutputs.size<=7) unmatchedOutputs += output //If no match -> ad it to the unmatched outputs
      case (ins, outs) => {
        for (in <- ins) {
          for (out <- outs) {
            graphEditor.addEdge(in.transactionID, new DownstreamTransactionEdge(out.transactionID))
            graphEditor.addEdge(out.transactionID, new UpstreamTransactionEdge(in.transactionID))
          }
        }
        unmatchedInputs --= ins
        unmatchedOutputs --= outs
      }
    }

  }

  //To be defined depending on the actual use case
  def findMatchingTransactions(newOutPut: TransactionOutput, outputs: Iterable[TransactionOutput], inputs: Iterable[TransactionInput]): (Iterable[TransactionInput], Iterable[TransactionOutput])

}