package com.signalcollect.fraudppuccino.componentdetection

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.SpecificationWithJUnit
import com.signalcollect.GraphBuilder
import com.signalcollect.fraudppuccino.repeatedanalysis.RepeatedAnalysisVertex
import com.signalcollect.fraudppuccino.structuredetection._

@RunWith(classOf[JUnitRunner])
class ComponentAlgorithmSpec extends SpecificationWithJUnit {

  "component algorithms" should {
    "work for sink counting" in {
      val graph = GraphBuilder.build
      //(ComponentMember ID, Component ID, successor (< 0 i.e. no successor for this vertex), src account, target account)
      val componentMembers = List((1, 5, List(2,3), 20, 21), (2, 5, List(), 21, 22), (3, 5, List(4,5), 21, 23), (4,5,List(), 23, 22), (5,5,List(), 23, 24))

      componentMembers.foreach(componentMember => {
        val vertex = new RepeatedAnalysisVertex(componentMember._1)
        vertex.storeAttribute("component", componentMember._2)
        vertex.storeAttribute("time", 100l)
        vertex.storeAttribute("value", 1000l)
        vertex.storeAttribute("src", componentMember._4)
        vertex.storeAttribute("target", componentMember._5)


        if (componentMember._1 == componentMember._2) {
          vertex.setAlgorithmImplementation(v => new ComponentMaster(v))
        } else {
          vertex.setAlgorithmImplementation(v => new ComponentMember(v))
        }

        graph.addVertex(vertex)
      })

      componentMembers.foreach(componentMember => {
        for(successorId <- componentMember._3) {
        	graph.addEdge(componentMember._1, new DownstreamTransactionEdgeWrapper(successorId))
        	graph.addEdge(successorId, new UpstreamTransactionEdgeWrapper(componentMember._1))
        }
      })
      
      //Members register themselves with the master
      graph.recalculateScores
      graph.execute
      
      var masterSinkCount: Int = 0
      
      val extendedSinkCondition: Any => Boolean = sinkCount => {
        val count = sinkCount.asInstanceOf[Int]
        masterSinkCount = count
        count > 0 
      } 
      
      graph.sendSignal(ComponentWorkflow(Array((ComponentAlgorithmParser.algorithms("sinks"), extendedSinkCondition))), 5	, None)
      
      graph.recalculateScores
      graph.execute
      
      masterSinkCount === 2
      
      
    }
  }
}