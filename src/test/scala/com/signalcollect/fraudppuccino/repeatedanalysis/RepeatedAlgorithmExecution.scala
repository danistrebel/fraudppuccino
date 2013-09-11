package com.signalcollect.fraudppuccino.repeatedanalysis

import org.specs2.mutable._
import com.signalcollect.GraphBuilder
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import com.signalcollect.fraudppuccino.repeatedanalysis.algorithms.SSSPAlgorithm
import com.signalcollect.StateForwarderEdge
import com.signalcollect.fraudppuccino.repeatedanalysis.algorithms.PageRankAlgorithm

@RunWith(classOf[JUnitRunner])
class RepeatedAlgorithmExecution extends SpecificationWithJUnit {
  "Repeated Analysis Algorithms" should {

    sequential

    //Graph setup

    val v1 = new RepeatedAnalysisVertex(1)
    val v2 = new RepeatedAnalysisVertex(2)
    val v3 = new RepeatedAnalysisVertex(3)
    val v4 = new RepeatedAnalysisVertex(4)
    val v5 = new RepeatedAnalysisVertex(5)
    val vertices = List(v1, v2, v3, v4, v5)

    val graph = GraphBuilder.build

    " build a graph for repeated executions of algorithms " in {

      for (vertex <- vertices) {
        graph.addVertex(vertex)
      }

      graph.addEdge(v1.id, new StateForwarderEdge(v2.id))
      graph.addEdge(v2.id, new StateForwarderEdge(v3.id))
      graph.addEdge(v3.id, new StateForwarderEdge(v4.id))
      graph.addEdge(v4.id, new StateForwarderEdge(v3.id))
      graph.addEdge(v4.id, new StateForwarderEdge(v5.id))
      graph.addEdge(v1.id, new StateForwarderEdge(v5.id))

      v1.state mustEqual None
    }

    " retain the states of multiple exectutions of the same algorithm " in {

      var ssspAlgorithm: RepeatedAnalysisVertex[_] => VertexAlgorithm = vertex => new SSSPAlgorithm(vertex)

      // Repeated SSSP execution
      val sources = List(v1, v2)
      var iteration = 1

      for (source <- sources) {
        for (vertex <- vertices) {
          vertex.setAlgorithmImplementation(ssspAlgorithm)
        }
        source.setState(0)
        graph.recalculateScores

        graph.execute

        for (vertex <- vertices) {
          vertex.retainState("SSSP" + iteration)
        }

        iteration += 1

      }

      // Test Retained Results
      v1.getResult("SSSP1").get.asInstanceOf[Int] === 0
      v1.getResult("SSSP2").get.asInstanceOf[Int] === Int.MaxValue

      v2.getResult("SSSP1").get.asInstanceOf[Int] === 1
      v2.getResult("SSSP2").get.asInstanceOf[Int] === 0

      v3.getResult("SSSP1").get.asInstanceOf[Int] === 2
      v3.getResult("SSSP2").get.asInstanceOf[Int] === 1

      v4.getResult("SSSP1").get.asInstanceOf[Int] === 3
      v4.getResult("SSSP2").get.asInstanceOf[Int] === 2

      v5.getResult("SSSP1").get.asInstanceOf[Int] === 1
      v5.getResult("SSSP2").get.asInstanceOf[Int] === 3

    }

    " be able to clear intermediary results " in {
      for (vertex <- vertices) {
        vertex.dropState("SSSP1")
      }

      v1.getResult("SSSP1") === None
      v2.getResult("SSSP1") === None
      v3.getResult("SSSP1") === None
      v4.getResult("SSSP1") === None
    }
    
    " run an other kind of algorithm on the same graph" in {
    	var pageRank: RepeatedAnalysisVertex[_] => VertexAlgorithm = vertex => new PageRankAlgorithm(vertex)
    	
    	for (vertex <- vertices) {
    		vertex.setAlgorithmImplementation(pageRank)
    	}
    	
    	graph.recalculateScores
    	graph.execute
    	
    	for (vertex <- vertices) {
          vertex.retainState("PR")
        }
    	
    	v1.getResult("PR") must not be None
    	v2.getResult("PR") must not be None
    	v3.getResult("PR") must not be None
    	v4.getResult("PR") must not be None
    }
  }
  

}