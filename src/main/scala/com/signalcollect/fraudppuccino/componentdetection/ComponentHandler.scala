package com.signalcollect.fraudppuccino.componentdetection

import akka.actor.Actor
import com.signalcollect.configuration.ActorSystemRegistry
import akka.actor.Props
import scala.collection.mutable.ArrayBuffer
import com.signalcollect.GraphEditor
import com.signalcollect.fraudppuccino.visualization.FraudppuchinoServer

/**
 * Handles components that were found in the graph
 */
class ComponentHandler(graphEditor: GraphEditor[Any, Any]) extends Actor {
  
  val visualizationServer = FraudppuchinoServer()

  val components = ArrayBuffer[Any]()

  def receive = {
    case ComponentAnnouncement(componentId) => {
      components += componentId
      graphEditor.sendSignal(ComponentSizeQuery, componentId, None)
    }
    case ComponentSizeReply(componentId, componentSize) => {
      if(componentSize<2) {
        dropComponent(componentId)
      } else {
        graphEditor.sendSignal(ComponentSerialization, componentId, None)

      }
    }
    
    case ComponentSerializationReply(componentJSON) =>  {
      visualizationServer.sendResult(componentJSON)
    }
    
  }
  
  def dropComponent(componentId: Any) {
    graphEditor.sendSignal(ComponentElimination, componentId, None)
  }
}