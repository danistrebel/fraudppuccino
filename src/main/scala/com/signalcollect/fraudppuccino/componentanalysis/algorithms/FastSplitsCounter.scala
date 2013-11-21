package com.signalcollect.fraudppuccino.componentanalysis.algorithms

import com.signalcollect.fraudppuccino.repeatedanalysis._
import com.signalcollect._
import com.signalcollect.fraudppuccino.structuredetection.DownstreamTransactionPatternEdge
import scala.collection.mutable.ArrayBuffer

/**
 * Tests if this transaction is split amongst other transactions within a predefined amount of time
 * @param duration maxDuration between this transaction and the latest split
 * @param minSplits that a transaction is split into
 */
class FastSplits(vertex: RepeatedAnalysisVertex[_], duration: Long = 86400, minSplits: Int = 3) extends VertexAlgorithm(vertex) with TransactionRelationshipExplorer {

  var isFastSplit = false

  def getState = isFastSplit

  val repliesFromSplits = ArrayBuffer[Long]()

  def setState(state: Any) = {
    state match {
      case split: Boolean => isFastSplit = split
      case _ =>
    }
  }

  def deliverSignal(signal: Any, sourceId: Option[Any], graphEditor: GraphEditor[Any, Any]) = {
    signal match {
      case RequestTransactionTime => graphEditor.sendSignal(time, sourceId.get, Some(vertex.id))
      case splitTimeStamp: Long => {
        repliesFromSplits += splitTimeStamp
        if (countSuccessors == repliesFromSplits.size) {
          isFastSplit = repliesFromSplits.forall(splitTime => splitTime-time < duration)       
        }
      }
      case _ =>
    }
    true
  }

  def executeSignalOperation(graphEditor: GraphEditor[Any, Any], outgoingEdges: Iterable[(Any, EdgeMarker)]) {
    if (isSplitter) {
      outgoingEdges.filter(edge => edge._2 == DownstreamTransactionPatternEdge).foreach(edge => graphEditor.sendSignal(RequestTransactionTime, edge._1, Some(vertex.id)))
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

case object RequestTransactionTime