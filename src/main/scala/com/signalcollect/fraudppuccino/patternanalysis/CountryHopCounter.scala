package com.signalcollect.fraudppuccino.patternanalysis

import com.signalcollect.fraudppuccino.repeatedanalysis._
import com.signalcollect._
import com.signalcollect.fraudppuccino.structuredetection.DownstreamTransactionPatternEdge
import com.signalcollect.fraudppuccino.structuredetection.UpstreamTransactionPatternEdge

/**
 * This counts the number of cross country transactions within a pattern. 
 * In back and forth hopping each spanning transaction is counted.
 */ 
class CountryHopCounter(vertex: RepeatedAnalysisVertex[_]) extends VertexAlgorithm(vertex) with TransactionRelationshipExplorer {

  val isXcountry = vertex.getResult("xCountry").getOrElse(false).asInstanceOf[Boolean] //is this transaction cross country
  
  var countryHops = if(isXcountry) 1 else 0

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
        val newHops = if(isXcountry) signaledHops +1 else signaledHops
        if (newHops > countryHops) {
        countryHops = newHops
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
