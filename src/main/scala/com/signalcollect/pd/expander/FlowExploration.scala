package com.signalcollect.pd.expander

import com.signalcollect._
import com.signalcollect.pd.analysis.VertexAlgorithm
import com.signalcollect.pd.analysis.RepeatedAnalysisVertex
import com.signalcollect.pd.analysis.TransactionSignature
import com.signalcollect.btc.transactiongraph.Transaction
import com.signalcollect.btc.transactiongraph.Transaction

class FlowExploration(vertex: RepeatedAnalysisVertex[_]) extends VertexAlgorithm {

  var incommingTx = List[TransactionSignature]() //Source ID, Input Value, Time

  def getState = incommingTx

  def setState(state: Any) = {
    state match {
      case x: List[TransactionSignature] => incommingTx = x
      case _ => throw new Exception("State not compatible")
    }
    scoreSignal = 1.0
  }

  def deliverSignal(signal: Any, sourceId: Option[Any], graphEditor: GraphEditor[Any, Any]) = {
    signal match {
      case txSig: TransactionSignature =>
        incommingTx = txSig :: incommingTx; false
      case _ => throw new Exception("Signal Type not compatible")
    }
  }

  def executeSignalOperation(graphEditor: GraphEditor[Any, Any], outgoingEdges: Iterable[Edge[_]]) {
	  for (tx <- outgoingEdges) {
	    tx.executeSignalOperation(vertex, graphEditor)
	  }
	  scoreSignal = 0.0
  }

  def executeCollectOperation(graphEditor: GraphEditor[Any, Any]) = {
	  
  }

  var scoreSignal = 1.0

  def scoreCollect: Double = {
    1.0
  }

  def noitfyTopologyChange {
    //Not considered
  }

}