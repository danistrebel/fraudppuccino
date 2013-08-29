package com.signalcollect.fraudppucchino.repeatedanalysis

import com.signalcollect._
import com.signalcollect.interfaces._

class Transaction[TargetId](val targetId: TargetId, val payload: Any, val timestap: Long) extends Edge[TargetId] {

  type Signal = Any

  var source: Source = _

  def id = EdgeId(sourceId.toString, targetId.toString + "," + timestap) //HACK to allow for multiple edges

  def weight: Double = 1 //not needed but required by the Edge interface.

  val cachedTargetIdHashCode = targetId.hashCode

  def executeSignalOperation(sourceVertex: Vertex[_, _], graphEditor: GraphEditor[Any, Any]) {
    executeSignalOperation(sourceVertex, graphEditor, TransactionSignature(payload, timestap)) 
  }
  
  def executeSignalOperation(sourceVertex: Vertex[_, _], graphEditor: GraphEditor[Any, Any], signal: Any) {
    graphEditor.sendToWorkerForVertexIdHash(SignalMessage(targetId, Some(sourceId), signal), cachedTargetIdHashCode) 
  }

  def onAttach(sourceVertex: Vertex[_, _], graphEditor: GraphEditor[Any, Any]) = {
    source = sourceVertex.asInstanceOf[Source]
  }
}

case class TransactionSignature(payload: Any, timestamp: Long)