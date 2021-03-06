package com.signalcollect.fraudppuccino.componentanalysis.algorithms

import com.signalcollect.fraudppuccino.repeatedanalysis._
import com.signalcollect._
import com.signalcollect.fraudppuccino.repeatedanalysis.EdgeMarkers._
import scala.collection.mutable.ArrayBuffer

/**
 * Tests if this transaction is split in equal shares
 * @param precision percentage that a split is allowed to vary from the fair share
 * @param minSplits that a transaction is split into
 */
case class EqualSplits(vertex: RepeatedAnalysisVertex[_], precision: Double = 0.1, minSplits: Int = 3) extends VertexAlgorithm with TransactionRelationshipExplorer {

  var isEqualSplit = false

  def getState = isEqualSplit

  val repliesFromSplits = ArrayBuffer[Long]()

  def setState(state: Any) = {
    state match {
      case split: Boolean => isEqualSplit = split
      case _ =>
    }
  }

  def deliverSignal(signal: Any, sourceId: Option[Any], graphEditor: GraphEditor[Any, Any]) = {
    signal match {
      case RequestTransactionValue => graphEditor.sendSignal(value, sourceId.get, Some(vertex.id))
      case splittedValue: Long => {
        repliesFromSplits += splittedValue
        if (countSuccessors == repliesFromSplits.size) {
          val equalSplitSize = value.toDouble / repliesFromSplits.size
          isEqualSplit = repliesFromSplits.forall(split => Math.abs(split - equalSplitSize) < precision * equalSplitSize)
        }
      }
      case _ =>
    }
    true
  }

  def executeSignalOperation(graphEditor: GraphEditor[Any, Any], outgoingEdges: Iterable[(Any, EdgeMarker)]) {
    if (isSplitter) {
      outgoingEdges.filter(edge => edge._2 == DownstreamTransactionPatternEdge).foreach(edge => graphEditor.sendSignal(RequestTransactionValue, edge._1, Some(vertex.id)))
    }
    scoreSignal = 0
  }

  def executeCollectOperation(graphEditor: GraphEditor[Any, Any]) = {
  }

  var scoreSignal = if (isSplitter && countSuccessors >= minSplits) 1.0 else 0.0

  var scoreCollect = 0.0

  def notifyTopologyChange {
  }
}

case object RequestTransactionValue