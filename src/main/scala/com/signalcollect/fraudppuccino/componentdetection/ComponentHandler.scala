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

  /**
   * Client interface to visualize the results to the user
   */ 
  val visualizationServer = FraudppuchinoServer(this)

  /**
   * Stores all the components and their current stage in the processing pipeline
   */
  val components = Map[Any, Int]()

  /**
   * Ordered list of processing steps that have to be passed by each component in order to count as a result.  
   */
  val componentWorkFlow: ArrayBuffer[(HandlerRequest, Any => Boolean)] = ArrayBuffer()

  def receive = {
    
    //Adds a new work flow step by parsing the work flow specification
    case WorkFlowStep(workFlow) => componentWorkFlow += ComponentAlgorithms.parseWorkFlowStep(workFlow)
    
    // Registration message from a component master 
    case ComponentAnnouncement(componentId) => {
      components += ((componentId, 0))
      graphEditor.sendSignal(componentWorkFlow(0)._1, componentId, None)
    }
    
    // Reply from the component master after executing a request
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
    // Serialized Information about a component and all its members
    case ComponentSerializationReply(componentJSON) => {
      println(componentJSON)
      visualizationServer.sendResult(componentJSON)
    }

  }
  
  /**
   * Executes the next step of the work flow or requests the serialized version of the component if the end of the work flow is reached. 
   */ 
  def executeNextInWorkFlow(componentId: Any) {
    val nextIndex = components(componentId) + 1 
    if(nextIndex>=componentWorkFlow.size) { //reached the last step in the work flow
      graphEditor.sendSignal(ComponentSerialization, componentId, None)
    } 
    else {
      components(componentId) = nextIndex
      graphEditor.sendSignal(componentWorkFlow(nextIndex)._1, componentId, None)
    }
    
  }

  /**
   * Removes a component from the work flow and sends a request to remove it from the processing graph.
   */ 
  def dropComponent(componentId: Any) {
    components -= componentId
    graphEditor.sendSignal(ComponentElimination, componentId, None)
  }
}