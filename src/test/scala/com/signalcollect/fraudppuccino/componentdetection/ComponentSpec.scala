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
import com.signalcollect.fraudppuccino.resulthandling._
import com.signalcollect.fraudppuccino.structuredetection._
import com.signalcollect.fraudppuccino.componentanalysis._
import com.signalcollect.fraudppuccino.repeatedanalysis.EdgeMarkers._

@RunWith(classOf[JUnitRunner])
class ComponentSpecs extends SpecificationWithJUnit {

  val graph = GraphBuilder.build

  "the component handler" should {
    "report the component master of a chain " in {
      //Register a component handler
      val system = ActorSystemRegistry.retrieve("SignalCollect").get
      val componentHandler = system.actorOf(Props(new ComponentHandler(graph)), "componentHandler")
      system.actorFor("akka://SignalCollect/user/componentHandler") === componentHandler

      //Specify the work flow
      val handlerRef = system.actorFor("akka://SignalCollect/user/componentHandler")
      handlerRef ! WorkFlowStep("SIZE > 6")
      handlerRef ! WorkFlowStep("DEPTH > 5")
      handlerRef ! RegisterResultHandler(ComponentResultHandlerFactory("DUMMY"))
      Thread.sleep(500l)

      //(ComponentMember ID, Component ID, successor (< 0 i.e. no successor for this vertex))
      val componentMembers = List((1, 1, 2), (2, 1, 3), (3, 1, 5), (4, 4, -1), (5, 1, 6), (6, 1, 7), (7, 1, 8), (8, 1, -1))

      componentMembers.foreach(componentMember => {
        val vertex = new RepeatedAnalysisVertex(componentMember._1)
        vertex.storeAttribute("component", componentMember._2)
        vertex.storeAttribute("time", 100l)
        vertex.storeAttribute("value", 1000l)

        if (componentMember._3 > 0) {
          vertex.outgoingEdges += ((componentMember._3, DownstreamTransactionPatternEdge))
        }

        if (componentMember._1 == componentMember._2) {
          vertex.setAlgorithmImplementation(v => new ComponentMaster(v))
        } else {
          vertex.setAlgorithmImplementation(v => new ComponentMember(v))
        }

        graph.addVertex(vertex)
      })

      graph.recalculateScores
      graph.execute
      graph.recalculateScores
      graph.execute

      //Simulate next computation step
      graph.foreachVertexWithGraphEditor(graphEditor => vertex =>
        vertex.deliverSignal(Array[Long](0l, 0l), None, graphEditor))

      graph.recalculateScores
      graph.execute

      //Simulate next computation step
      graph.foreachVertexWithGraphEditor(graphEditor => vertex =>
        vertex.deliverSignal(Array[Long](0l, 0l), None, graphEditor))

      graph.recalculateScores
      graph.execute

      Thread.sleep(500l) //make sure the asynchronous messages are received.

      //Check the sizes of the reported components
      DummyResultsHandler.reportedComponents(1) === 7
      DummyResultsHandler.reportedComponents.get(4) === None
    }
  }
}