package com.signalcollect.fraudppuccino.componentdetection

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.SpecificationWithJUnit
import com.signalcollect._
import com.signalcollect.fraudppuccino.repeatedanalysis._
import akka.event.Logging.LogLevel
import akka.event.Logging
import com.signalcollect.configuration.ActorSystemRegistry
import akka.actor.Props
import akka.actor.Actor
import scala.util.parsing.json.JSON

@RunWith(classOf[JUnitRunner])
class ComponentSpecs extends SpecificationWithJUnit {

  sequential
  val graph = GraphBuilder.build

  "the component handler" should {
    "register itself in the actor system " in {
      //Register a component handler
      val system = ActorSystemRegistry.retrieve("SignalCollect").get
      val componentHandler = system.actorOf(Props(new ComponentHandler(graph)), "componentHandler")
      system.actorFor("akka://SignalCollect/user/componentHandler") === componentHandler
    }
  }

  "component members" should {

    "should remain in the graph iff they pass all steps the workflow " in {
      
      //Specify the work flow
      val system = ActorSystemRegistry.retrieve("SignalCollect").get
      val handlerRef = system.actorFor("akka://SignalCollect/user/componentHandler")
      handlerRef ! WorkFlowStep("SIZERETAIN > 6")
      handlerRef ! RegisterResultHandler(DummyResultsHandler)
      
      //(ComponentMember ID, Component ID)
      val componentMembers = List((1, 1), (2, 1), (3, 1), (4, 4), (5, 1), (6, 1), (7, 1), (8, 1))

      componentMembers.foreach(componentMember => {
        val vertex = new RepeatedAnalysisVertex(componentMember._1)
        vertex.storeAttribute("component", componentMember._2)
        vertex.storeAttribute("time", 0l)
        vertex.storeAttribute("value", 1000l)


        if (componentMember._1 == componentMember._2) {
          vertex.setAlgorithmImplementation(v => new ComponentMaster(v))
        } else {
          vertex.setAlgorithmImplementation(v => new ComponentMember(v))
        }

        graph.addVertex(vertex)
      })

      graph.recalculateScores
      graph.execute
      
      Thread.sleep(500l)
      
      DummyResultsHandler.reportedComponents(1) === 7
      DummyResultsHandler.reportedComponents.get(4) === None
    }
  }
}

case object DummyResultsHandler extends ComponentResultHandler {
  
  var reportedComponents: scala.collection.mutable.Map[Int, Int] = scala.collection.mutable.Map()
  
  def processResult(jsonData: String) {
      val parsed = JSON.parseFull(jsonData).get.asInstanceOf[Map[String, Any]]
      reportedComponents += ((parsed("id").asInstanceOf[Double].intValue, parsed("members").asInstanceOf[List[_]].size))
  }
}