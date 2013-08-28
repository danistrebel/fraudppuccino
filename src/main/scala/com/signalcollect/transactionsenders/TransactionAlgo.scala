package com.signalcollect.transactionsenders

import com.signalcollect._
import com.signalcollect.pd.analysis.VertexAlgorithm
import com.signalcollect.pd.analysis.RepeatedAnalysisVertex

class TransactionLinker(vertex: RepeatedAnalysisVertex[_]) extends VertexAlgorithm {

  val value = vertex.getResult("value").getOrElse(0).asInstanceOf[Int]
  val time = vertex.getResult("value").getOrElse(0).asInstanceOf[Int]
  
  def getState = null

  def setState(state: Any) = {
    scoreSignal = 1.0
  }

  def deliverSignal(signal: Any, sourceId: Option[Any], graphEditor: GraphEditor[Any, Any]) = {
    signal match {
      case txSignature: TransactionSignature => {
        if (Math.abs(txSignature.value - this.value) / this.value < 0.1) { //max 10% difference
          graphEditor.addEdge(txSignature.transactionID, new TransactionPatternEdge(vertex.id.asInstanceOf[Int]))
        }
        true
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

  def executeCollectOperation(graphEditor: GraphEditor[Any, Any]) = {

  }

  var scoreSignal = 1.0

  def scoreCollect: Double = {
    0.0
  }

  def noitfyTopologyChange {
    //Not considered
  }

  def signature: TransactionSignature = {
    TransactionSignature(vertex.id.asInstanceOf[Int], this.value, this.time)
  }
  
  override def toString: String = {
    value + " at time: " + time
  }
}