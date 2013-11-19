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
    
    "work for x country counting" in {
      val graph = GraphBuilder.build
      //(ComponentMember ID, Component ID, successor (< 0 i.e. no successor for this vertex), src account, target account)
      val componentMembers = List((1, 5, List(2), true), (2, 5, List(5), true), (3, 5, List(4), false), (4,5,List(5), true), (5,5,List(), true))

      componentMembers.foreach(componentMember => {
        val vertex = new RepeatedAnalysisVertex(componentMember._1)
        vertex.storeAttribute("component", componentMember._2)
        vertex.storeAttribute("time", 100l)
        vertex.storeAttribute("value", 1000l)
        vertex.storeAttribute("xCountry", componentMember._4)        


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
      
      var maxCountryHops: Int = 0
      
      val extndedCountryHopCondition: Any => Boolean = countryHops => {
        val count = countryHops.asInstanceOf[Int]
        maxCountryHops = count
        count > 0 
      } 
      
      graph.sendSignal(ComponentWorkflow(Array((ComponentAlgorithmParser.algorithms("countryhops"), extndedCountryHopCondition))), 5	, None)
      
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
      
      
      maxCountryHops === 2
    }
    
    "work for cicle counting" in {
      val graph = GraphBuilder.build
      //(ComponentMember ID, Component ID, successor (< 0 i.e. no successor for this vertex), src account, target account)
      val componentMembers = List((1, 5, List(2,3), 20, 21), (2, 5, List(5), 21, 22), (3, 5, List(4,5), 21, 23), (4,5,List(5), 23, 22), (5,5,List(), 22, 20))

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
      
      var masterCircleCount: Int = 0
      
      val extendedCircleCondition: Any => Boolean = circleCount => {
        val count = circleCount.asInstanceOf[Int]
        masterCircleCount = count
        count > 0 
      } 
      
      graph.sendSignal(ComponentWorkflow(Array((ComponentAlgorithmParser.algorithms("circlemembers"), extendedCircleCondition))), 5	, None)
      
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
      
      masterCircleCount === 1
    }
    
    "work for equal splits counting" in {
      val graph = GraphBuilder.build
      //(ComponentMember ID, Component ID, successor (< 0 i.e. no successor for this vertex), tx value)
      val componentMembers = List((1, 5, List(2,3,4,5), 1000l), (2, 5, List(), 250l), (3, 5, List(), 250l), (4,5,List(), 250l), (5,5,List(), 250l))

      componentMembers.foreach(componentMember => {
        val vertex = new RepeatedAnalysisVertex(componentMember._1)
        vertex.storeAttribute("component", componentMember._2)
        vertex.storeAttribute("time", 100l)
        vertex.storeAttribute("value", componentMember._4)


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
      
      var masterSplitCount: Int = 0
      
      val extendedEqualSplitCountCondition: Any => Boolean = splitCount => {
        val count = splitCount.asInstanceOf[Int]
        masterSplitCount = count
        count > 0 
      } 
      
      graph.sendSignal(ComponentWorkflow(Array((ComponentAlgorithmParser.algorithms("fairsplits"), extendedEqualSplitCountCondition))), 5	, None)
      
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
      
      masterSplitCount === 1
    }
  }
}