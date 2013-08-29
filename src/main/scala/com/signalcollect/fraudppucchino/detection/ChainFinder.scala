package com.signalcollect.fraudppucchino.detection

import com.signalcollect.fraudppuchino.repeatedanalysis.VertexAlgorithm
import com.signalcollect._
import scala.collection.mutable.MutableList

class ChainFinder(vertex: Vertex[_, _]) extends VertexAlgorithm {

  var endedStreams = MutableList[MutableList[Int]]()

  def getState = endedStreams

  def setState(state: Any) = {
    state match {
      case list: MutableList[MutableList[Int]] => endedStreams = list
      case _ => throw new Exception("State not supported")
    }
    scoreSignal = 1.0
  }

  def deliverSignal(signal: Any, sourceId: Option[Any], graphEditor: GraphEditor[Any, Any]) = {
    false
  }

  def executeSignalOperation(graphEditor: GraphEditor[Any, Any], outgoingEdges: Iterable[Edge[_]]) {
    outgoingEdges.filter(_.isInstanceOf[TransactionPatternEdge]).foreach(v => {

    })
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