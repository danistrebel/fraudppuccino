package com.signalcollect.fraudppuccino.componentanalysis.algorithms

import com.signalcollect.fraudppuccino.repeatedanalysis._
import com.signalcollect._
import com.signalcollect.fraudppuccino.structuredetection.DownstreamTransactionPatternEdge

/**
 * This counts the number of cross country transactions within a pattern.
 * The number is determines by the smallest number of country hops from any
 * connected source transaction to this vertex.
 * In back and forth hopping each spanning transaction is counted.
 */
class CountryHopCounter(vertex: RepeatedAnalysisVertex[_]) extends VertexAlgorithm(vertex) with TransactionRelationshipExplorer {

  var countryHops = ownXcountryCount
  var smallestHopsReceived = Int.MaxValue

  def ownXcountryCount = if (isXcountry) 1 else 0

  def getState = countryHops

  def setState(state: Any) = {
    state match {
      case newHop: Int => countryHops = newHop
      case _ =>
    }
  }

  def deliverSignal(signal: Any, sourceId: Option[Any], graphEditor: GraphEditor[Any, Any]) = {
    signal match {
      case signaledHops: Int =>
        if (signaledHops < smallestHopsReceived) {
          smallestHopsReceived = signaledHops
          countryHops = smallestHopsReceived + ownXcountryCount
          scoreSignal = 1.0
        }
      case _ =>
    }
    true
  }

  def executeSignalOperation(graphEditor: GraphEditor[Any, Any], outgoingEdges: Iterable[(Any, EdgeMarker)]) {
    outgoingEdges.filter(edge => edge._2 == DownstreamTransactionPatternEdge).foreach(edge => graphEditor.sendSignal(countryHops, edge._1, Some(vertex.id)))
    scoreSignal = 0
  }

  def executeCollectOperation(graphEditor: GraphEditor[Any, Any]) = {
  }

  var scoreSignal = if (isPatternSource) 1.0 else 0.0

  var scoreCollect = 0.0

  def notifyTopologyChange {
  }
}
