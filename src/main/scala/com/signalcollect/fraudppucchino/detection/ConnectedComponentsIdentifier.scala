package com.signalcollect.fraudppucchino.detection

import com.signalcollect.fraudppucchino.repeatedanalysis._
import com.signalcollect._

/**
 * Runs a specialized for of label propagation
 * to label all connected sub-patterns with the
 * smallest id of its members. 
 * 
 * In later steps this label allows the user to
 * determine if two components are linked to each
 * other or not.
 */ 
class ConnectedComponentsIdentifier(vertex: RepeatedAnalysisVertex[_]) extends VertexAlgorithm {

  var componentId = vertex.id.asInstanceOf[Int]

  var scoreSignal = 1.0
  var scoreCollect = 0.0

  def getState: Any = componentId

  def setState(state: Any) {
    state match {
      case id: Int => componentId = id
      case _ => 
    }
  }

  def deliverSignal(signal: Any, sourceId: Option[Any], graphEditor: GraphEditor[Any, Any]): Boolean = {
    signal match {
      case id: Int => {
        if(id<componentId) {
          componentId = id
          scoreSignal = 1.0
        }
        
      }
    }
    false
  }

  def executeSignalOperation(graphEditor: GraphEditor[Any, Any], outgoingEdges: Iterable[(Any, EdgeMarker)]) = {
    for(edge <- outgoingEdges) {
      if(edge._2.isInstanceOf[TransactionPatternEdge]) {
        graphEditor.sendSignal(componentId, edge._1, Some(vertex.id))
      }
    }
    scoreSignal = 0.0
  }

  def executeCollectOperation(graphEditor: GraphEditor[Any, Any]) = {
    //allways directly collected
  }

  def noitfyTopologyChange = {}
}