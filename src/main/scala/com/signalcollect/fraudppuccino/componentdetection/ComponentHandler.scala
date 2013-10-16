package com.signalcollect.fraudppuccino.componentdetection

import akka.actor.Actor
import com.signalcollect.configuration.ActorSystemRegistry
import akka.actor.Props
import scala.collection.mutable.ArrayBuffer
import com.signalcollect.GraphEditor

/**
 * Handles components that were found in the graph
 */
class ComponentHandler(graphEditor: GraphEditor[Any, Any]) extends Actor {

  val components = ArrayBuffer[Any]()

  def receive = {
    case ComponentAnnouncement(componentId) => {
      components += componentId
      graphEditor.sendSignal(ComponentSizeQuery, componentId, None)
    }
    case ComponentSizeReply(componentId, componentSize) => {
      if(componentSize<2) {
        graphEditor.sendSignal(ComponentElimination, componentId, None)
      }
    }
  }
  
  def dropComponent(componentId: Any) {
    graphEditor.sendSignal(ComponentElimination, componentId, None)
  }
}