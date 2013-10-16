package com.signalcollect.fraudppuccino.componentdetection

import com.signalcollect.fraudppuccino.repeatedanalysis.RepeatedAnalysisVertex
import scala.collection.mutable.ArrayBuffer
import com.signalcollect._
import com.signalcollect.configuration.ActorSystemRegistry
import akka.actor.ActorRef

/**
 *  Serves as the main point of access to a connected component.
 */
class ComponentMaster(vertex: RepeatedAnalysisVertex[_]) extends ComponentMember(vertex) {

  //Stores the Ids of all the members of the component that it represents
  //will include itself as a member i.e. members.size >= 1
  val members = ArrayBuffer[Any]()
  
  var handler: ActorRef = null
  

  override def deliverSignal(signal: Any, sourceId: Option[Any], graphEditor: GraphEditor[Any, Any]) = {
    
    if(handler == null) {
    	val system = ActorSystemRegistry.retrieve("SignalCollect").get
    	handler = system.actorFor("akka://SignalCollect/user/componentHandler")
    	graphEditor.sendToActor(handler, ComponentAnnouncement(vertex.id))
    }
	  
    
    signal match {
      case ComponentMemberRegistration =>
        members += sourceId.get; true
      case ComponentSizeQuery => {
        graphEditor.sendToActor(handler, ComponentSizeReply(vertex.id, members.size))
        vertex.storeAttribute("componentSize", members.size)
        true
      }
      case ComponentElimination => {
        members.foreach(memberId => {
          graphEditor.sendSignal(ComponentMemberElimination, memberId, Some(vertex.id))
        })
        true
      }
      case _ => super.deliverSignal(signal, sourceId, graphEditor)
    }

  }
}