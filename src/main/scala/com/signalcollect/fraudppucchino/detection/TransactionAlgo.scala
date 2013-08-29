package com.signalcollect.fraudppucchino.detection

import com.signalcollect._
import com.signalcollect.fraudppucchino.repeatedanalysis.VertexAlgorithm
import com.signalcollect.fraudppucchino.repeatedanalysis.RepeatedAnalysisVertex
import scala.collection.mutable.LinkedList
import scala.collection.mutable.ArrayBuffer

class TransactionLinker(vertex: RepeatedAnalysisVertex[_]) extends VertexAlgorithm {

  val candidateInputs = new ArrayBuffer[TransactionSignature]()

  val value = vertex.getResult("value").getOrElse(0).asInstanceOf[Int]
  val time = vertex.getResult("value").getOrElse(0).asInstanceOf[Int]

  def getState = None

  def setState(state: Any) = {
    scoreSignal = 1.0
  }

  def deliverSignal(signal: Any, sourceId: Option[Any], graphEditor: GraphEditor[Any, Any]) = {
    signal match {
      case txSignature: TransactionSignature => {
        if (candidateInputs.size < 7) { //To make sure we don't explode
          candidateInputs.append(txSignature)
        }
        scoreCollect = 1.0
        false
      }
      case _ => throw new Exception("Signal Type not compatible")
    }

  }

  def executeSignalOperation(graphEditor: GraphEditor[Any, Any], outgoingEdges: Iterable[Edge[_]]) {
    for (edge <- outgoingEdges) {
      graphEditor.sendSignal(signature, edge.targetId, Some(edge.id.sourceId))
    }
    scoreSignal = 0.0
  }

  /**
   *
   */
  def executeCollectOperation(graphEditor: GraphEditor[Any, Any]) = {
    val matched = new ArrayBuffer[TransactionSignature]()
    val matchingInputs = findAllMatchingInputs
    if (!matchingInputs.isEmpty) {
      for (txSignature <- matchingInputs.head) {
        graphEditor.addEdge(txSignature.transactionID, new TransactionPatternEdge(vertex.id.asInstanceOf[Int]))
        matched += txSignature
      }
    }
    candidateInputs --= matched

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
  lazy val signature: TransactionSignature = TransactionSignature(vertex.id.asInstanceOf[Int], this.value, this.time)

  /**
   * Combines one or more incoming transactions of a node if they could be the source of this transaction
   */
  def findAllMatchingInputs: Iterable[List[TransactionSignature]] = {
    val allSuitableInputs = candidateInputs.filter(_.value < this.value)
    val subsets = allSuitableInputs.toSet.subsets
    subsets.toList.map(_.toList).filter(txCombo => areWithinTimeWindow(txCombo) && sumsUpToThisValue(txCombo))
  }

  /**
   * Matching combinations of transactions that preceded this transaction by max #windowsize days.
   */ 
  def areWithinTimeWindow(combination: Iterable[TransactionSignature], windowSize: Int = 5): Boolean = {
    combination.forall(otherTx => this.time - otherTx.time < windowSize && this.time - otherTx.time >= 0)
  }
 
  /**
   * Matching combinations have to sum up to the value of this transaction
   */
  def sumsUpToThisValue(combination: List[TransactionSignature], tolerance: Float = 0.1f): Boolean = {
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
    "links to " + vertex.outgoingEdges.values.filter(_.isInstanceOf[TransactionPatternEdge])
  }
}