package com.signalcollect.fraudppuccino.structuredetection

import com.signalcollect.GraphEditor
import com.signalcollect.fraudppuccino.repeatedanalysis.RepeatedAnalysisVertex

/**
 * Extension of the TransactionAnnoncer that notifies its source and target vertices when it times out.
 */
class UnsubscribingTransactionAnnouncer(vertex: RepeatedAnalysisVertex[_]) extends TransactionAnnouncer(vertex) {
  override def handleTimeout(graphEditor: GraphEditor[Any, Any]) {
    graphEditor.sendSignal(TransactionTimedOut, source, Some(id))
    graphEditor.sendSignal(TransactionTimedOut, target, Some(id))
    super.handleTimeout(graphEditor)
  }
}