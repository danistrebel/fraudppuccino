package com.signalcollect.fraudppucchino.repeatedanalysis

import com.signalcollect._
import com.signalcollect.fraudppucchino.repeatedanalysis.algorithms._


object RepeatedDemo extends App {
  var ssspAlgorithm: RepeatedAnalysisVertex[_] => VertexAlgorithm = vertex => new SSSPAlgorithm(vertex)
  var pageRank: RepeatedAnalysisVertex[_] => VertexAlgorithm = vertex => new PageRankAlgorithm(vertex)

  
  val v1 = new RepeatedAnalysisVertex(1)
  val v2 = new RepeatedAnalysisVertex(2)
  val v3 = new RepeatedAnalysisVertex(3)
  val v4 = new RepeatedAnalysisVertex(4)
  val v5 = new RepeatedAnalysisVertex(5)

  val vertices = List(v1, v2, v3, v4, v5)

  for (vertex <- vertices) {
    vertex.setAlgorithmImplementation(ssspAlgorithm)
  }

  v1.setState(0)

  val graph = GraphBuilder.build

  for (vertex <- vertices) {
    graph.addVertex(vertex)
  }

  graph.addEdge(v1.id, new StateForwarderEdge(v2.id))
  graph.addEdge(v2.id, new StateForwarderEdge(v3.id))
  graph.addEdge(v3.id, new StateForwarderEdge(v4.id))
  graph.addEdge(v4.id, new StateForwarderEdge(v3.id))
  graph.addEdge(v4.id, new StateForwarderEdge(v5.id))
  graph.addEdge(v5.id, new StateForwarderEdge(v1.id))

  val stats = graph.execute
  println(stats)
  graph.foreachVertex(println(_))

  graph.foreachVertex(_ match {
    case rav: RepeatedAnalysisVertex[_] => {
      rav.retainState("SSSP1")
      rav.setAlgorithmImplementation(pageRank)
    }
    case _ =>
  })

  graph.recalculateScores

  val stats2 = graph.execute
  println(stats2)

  for (vertex <- vertices) {
    vertex.retainState("PR")
  }

  graph.foreachVertex(println(_))
}