package com.signalcollect.fraudppuccino.detection

import com.signalcollect._
import com.signalcollect.fraudppuccino.repeatedanalysis._
import scala.collection.mutable.LinkedList
import scala.collection.mutable.ArrayBuffer

class TransactionLinker(vertex: RepeatedAnalysisVertex[_]) extends VertexAlgorithm {

  val candidateInputs = new ArrayBuffer[TransactionInput]()
  val candidateOutputs = new ArrayBuffer[TransactionOutput]()

  val value = vertex.getResult("value").getOrElse(0l).asInstanceOf[Long]
  val time = vertex.getResult("time").getOrElse(0l).asInstanceOf[Long]

  def getState = None

  def setState(state: Any) = {
    scoreSignal = 1.0
  }

  def deliverSignal(signal: Any, sourceId: Option[Any], graphEditor: GraphEditor[Any, Any]) = {
    signal match {
      case txInput: TransactionInput => {
        if (isIsPossibleInputByTime(txInput) && candidateInputs.size < 7) { //To make sure we don't explode
          candidateInputs.append(txInput)
          scoreCollect = 1.0
        }
        false
      }
      case txOutput: TransactionOutput => {
        if (candidateOutputs.size < 7) { //To make sure we don't explode
          candidateOutputs.append(txOutput)
          scoreCollect = 1.0
        }
        false
      }
      case _ => throw new Exception("Signal Type not compatible")
    }

  }

  /**
   * Initially signal the transactions signature to its receiver
   */
  def executeSignalOperation(graphEditor: GraphEditor[Any, Any], outgoingEdges: Iterable[(Any, EdgeMarker)]) {
    for (edge <- outgoingEdges) {
      graphEditor.sendSignal(inputSignature, edge._1, Some(vertex.id))
    }
    scoreSignal = 0.0
  }

  /**
   * Try to match the incoming signals and compute outputs and/or inputs of this transaction
   */
  def executeCollectOperation(graphEditor: GraphEditor[Any, Any]) = {

    //split inputs in candidates for (chains/aggregations, splits)
    val inOutTuple = candidateInputs.partition(_.value <= this.value)

    //send split candidates for evaluation at the splitter
    for (splitCandidate <- inOutTuple._2) {
      graphEditor.sendSignal(outputSignature, splitCandidate.transactionID, Some(vertex.id))
    }
    candidateInputs --= inOutTuple._2

    //test for inputs that this transaction can be composed of and link them (this transaction acts as chain element or aggregator)
    val matchingInputs = findAllMatchingSignals(inOutTuple._1)
    if (!matchingInputs.isEmpty) {
      for (txSignature <- matchingInputs.head) {
        graphEditor.addEdge(txSignature.transactionID, EdgeMarkerWrapper(vertex.id.asInstanceOf[Int], DownstreamTransactionPatternEdge))
        graphEditor.addEdge(vertex.id, EdgeMarkerWrapper(txSignature.transactionID, UpstreamTransactionPatternEdge))
        candidateInputs -= txSignature.asInstanceOf[TransactionInput]
      }
    }

    //test for outputs that can be summed up to this transaction (transaction acts as a splitter)
    val matchingOutputs = findAllMatchingSignals(candidateOutputs)
    if (!matchingOutputs.isEmpty) {
      for (txSignature <- matchingOutputs.head) {
        graphEditor.addEdge(vertex.id, EdgeMarkerWrapper(txSignature.transactionID, DownstreamTransactionPatternEdge))
        graphEditor.addEdge(txSignature.transactionID, EdgeMarkerWrapper(vertex.id.asInstanceOf[Int], UpstreamTransactionPatternEdge))
        candidateOutputs -= txSignature.asInstanceOf[TransactionOutput]
      }
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
   * Combines one or more incoming transactions of a node if they could be the source of this transaction
   */
  def findAllMatchingSignals(candidates: Iterable[TransactionSignal]): Iterable[List[TransactionSignal]] = {
    val subsets = candidates.toSet.subsets
    subsets.toList.map(_.toList).filter(txCombo => sumsUpToThisValue(txCombo))
  }

  /**
   * Inputs have to happen before or immediately with this transaction.
   * Inputs are considered within the window size.
   */
  def isIsPossibleInputByTime(inputCandidate: TransactionInput, windowSize: Long = 432000l): Boolean = {
     this.time - inputCandidate.time >= 0 && this.time - inputCandidate.time < windowSize
  }

  /**
   * Matching combinations have to sum up to the value of this transaction
   */
  def sumsUpToThisValue(combination: List[TransactionSignal], tolerance: Float = 0.1f): Boolean = {
    matchesThisValue(combination.foldLeft(0.0)(_ + _.value), tolerance)
  }

  /**
   * Is considered a match if differs at max 10% from another incoming transaction
   */
  def matchesThisValue(otherValue: Double, tolerance: Float): Boolean = (Math.abs(otherValue - this.value) / this.value) < tolerance

  /**
   * Logs debug for tx with id
   */
  def logFor(id: Int, msg: String) = if (vertex.id == id) println(msg)

  override def toString: String = {
    "links to " + vertex.outgoingEdges
  }
}