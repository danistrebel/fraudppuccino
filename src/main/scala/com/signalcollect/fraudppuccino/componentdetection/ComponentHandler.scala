package com.signalcollect.fraudppuccino.componentdetection

import akka.actor.Actor
import com.signalcollect.configuration.ActorSystemRegistry
import akka.actor.Props
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.Map
import com.signalcollect.GraphEditor
import com.signalcollect.fraudppuccino.repeatedanalysis._
import com.signalcollect.fraudppuccino.structuredetection._
import com.signalcollect.fraudppuccino.resulthandling.ComponentResultHandler

/**
 * Handles components that were found in the graph
 */
class ComponentHandler(graphEditor: GraphEditor[Any, Any]) extends Actor {

  /**
   * Client interface to visualize the results to the user
   */
  val resultHandler: ArrayBuffer[ComponentResultHandler] = ArrayBuffer()

  /**
   * Ordered list of processing steps that have to be passed by each component in order to count as a result.
   */
  val componentWorkFlow: ArrayBuffer[ComponentWorkflowStep] = ArrayBuffer()

  def receive = {
    
    case RegisterResultHandler(handler) => resultHandler += handler

    //Adds a new work flow step by parsing the work flow specification
    case WorkFlowStep(workFlow) => componentWorkFlow += ComponentAlgorithmParser.parseWorkFlowStep(workFlow)

    // Registration message from a component master 
    case ComponentAnnouncement(componentId) => {
      graphEditor.sendSignal(ComponentWorkflow(componentWorkFlow), componentId, None)
    }
    
    case ComponentResult(serializedComponent) => {
      resultHandler.foreach(_.processResult(serializedComponent))
    }
    
    case ComputationStatus(serializedStatus) => {
      resultHandler.foreach(_.processStatusMessage(serializedStatus))
    }
  }
}