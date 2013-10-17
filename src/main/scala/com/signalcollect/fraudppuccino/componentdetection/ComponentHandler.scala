package com.signalcollect.fraudppuccino.componentdetection

import akka.actor.Actor
import com.signalcollect.configuration.ActorSystemRegistry
import akka.actor.Props
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.Map
import com.signalcollect.GraphEditor
import com.signalcollect.fraudppuccino.visualization.FraudppuchinoServer

/**
 * Handles components that were found in the graph
 */
class ComponentHandler(graphEditor: GraphEditor[Any, Any]) extends Actor {

  val visualizationServer = FraudppuchinoServer()

  //Stores all the components and their current stage in the processing pipeline
  val components = Map[Any, Int]()

  type ResultFilter = PartialFunction[Any, Boolean]

  val sizeFilterStep = (ComponentSizeQuery, { case size: Int => size > 6; case _ => false }: PartialFunction[Any, Boolean])
  val componentWorkFlow: IndexedSeq[(HandlerRequest, ResultFilter)] = ArrayBuffer(sizeFilterStep)

  def receive = {
    case ComponentAnnouncement(componentId) => {
      components += ((componentId, 0))
      graphEditor.sendSignal(componentWorkFlow(0)._1, componentId, None)
    }
    case ComponentReply(componentId, reply) => {
      reply match {
        case Some(result) => {
          val currentIndex = components(componentId)
          if(componentWorkFlow(currentIndex)._2(result)) { //if result is accepted by the filter move to the next step in the work flow
            executeNextInWorkFlow(componentId)
          } else { //Result is filtered out
            dropComponent(componentId)
          }
        }
        case None => executeNextInWorkFlow(componentId)
      }
    }

    case ComponentSerializationReply(componentJSON) => {
      println(componentJSON)
      visualizationServer.sendResult(componentJSON)
    }

  }
  
  def executeNextInWorkFlow(componentId: Any) {
    val nextIndex = components(componentId) + 1 
    if(nextIndex>=componentWorkFlow.size) { //reached the last step in the workflow
      graphEditor.sendSignal(ComponentSerialization, componentId, None)
    } 
    else {
      components(componentId) = nextIndex
      graphEditor.sendSignal(componentWorkFlow(nextIndex)._1, componentId, None)
    }
    
  }

  def dropComponent(componentId: Any) {
    components -= componentId
    graphEditor.sendSignal(ComponentElimination, componentId, None)
  }
}