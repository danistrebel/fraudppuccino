package com.signalcollect.fraudppuccino.componentdetection

import com.signalcollect.fraudppuccino.repeatedanalysis.RepeatedAnalysisVertex
import scala.collection.mutable.ArrayBuffer
import com.signalcollect._

/**
 *  Serves as the main point of access to a connected component.
 */ 
class ComponentMaster(vertex: RepeatedAnalysisVertex[_]) extends ComponentMember(vertex) {
  
  //Stores the Ids of all the members of the component that it represents
  //includes itself as a member i.e. members.size >= 1
  val members = ArrayBuffer[Any](vertex.id)

  override def deliverSignal(signal: Any, sourceId: Option[Any], graphEditor: GraphEditor[Any, Any]) = {
    signal match {
      case ComponentMemberRegistration => members+=sourceId.get
      case _ => super.deliverSignal(signal, sourceId, graphEditor)
    }
    true
  }
}