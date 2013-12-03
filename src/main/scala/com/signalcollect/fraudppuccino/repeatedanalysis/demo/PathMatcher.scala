package com.signalcollect.fraudppuccino.repeatedanalysis.demo

import com.signalcollect._
import com.signalcollect.fraudppuccino.repeatedanalysis._
import com.signalcollect.fraudppuccino.repeatedanalysis.EdgeMarkers._ 


case class PathMatcher(vertex: RepeatedAnalysisVertex[_]) extends VertexAlgorithm {

  var unmatchedPatterns: List[RegexPattern] = List()
  var matches: List[RegexPattern] = List()

  def getState = unmatchedPatterns

  def setState(state: Any) = {
    state match {
      case pattern: RegexPattern => unmatchedPatterns = List(pattern)
      case _ => throw new Exception("State not compatible")
    }
  }

  def deliverSignal(signal: Any, sourceId: Option[Any], graphEditor: GraphEditor[Any, Any]): Boolean = {
    signal match {
      case pattern: RegexPattern =>
        unmatchedPatterns = pattern :: unmatchedPatterns; false
      case _ => throw new Exception("State not compatible")
    }
  }

  def executeSignalOperation(graphEditor: GraphEditor[Any, Any], outgoingEdges: Iterable[(Any, EdgeMarker)]) {
    for (edge <- outgoingEdges) {
      for (m <- matches) {
        graphEditor.sendSignal(m, edge._1, Some(vertex.id))
      }
    }
    matches = List()
  }

  def executeCollectOperation(graphEditor: GraphEditor[Any, Any]) {
    for (pattern <- unmatchedPatterns) {
      pattern.check(vertex) match {
        case p: NoMatch =>
        case p: TerminalMatch => println("Found Pattern")
        case p: PartialMatch => matches = p :: matches
      }
    }
  }

  def scoreSignal: Double = matches.size

  def scoreCollect: Double = unmatchedPatterns.size

  def notifyTopologyChange {}

}

abstract class RegexPattern {
  def check(vertex: RepeatedAnalysisVertex[_]): RegexPattern = {
    return new NoMatch
  }
}

case class NoMatch extends RegexPattern
case class TerminalMatch extends RegexPattern
case class PartialMatch extends RegexPattern