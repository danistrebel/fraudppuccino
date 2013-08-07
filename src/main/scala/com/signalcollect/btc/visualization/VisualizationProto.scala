package com.signalcollect.btc.visualization

import com.signalcollect.configuration.GraphConfiguration
import com.signalcollect.console.ConsoleServer
import com.signalcollect.GraphBuilder
import com.signalcollect.btc.transactiongraph.PublicAddress
import com.signalcollect.btc.transactiongraph.Transaction
import com.signalcollect.DefaultEdge
import com.signalcollect.StateForwarderEdge
import com.signalcollect.ExecutionConfiguration
import com.signalcollect.configuration.ExecutionMode
import com.signalcollect.examples.PageRankVertex
import com.signalcollect.examples.PageRankEdge 

object VisualizationProto extends App {

//  val graphConfig = new GraphConfiguration()
//  val console = new ConsoleServer(graphConfig)

  val graph = GraphBuilder.withConsole(true, 8081, "html/detective.html").build
  
  val pa1 = new PublicAddress(1)
  val pa2 = new PublicAddress(2)
  val pa3 = new PublicAddress(3)

  val tr1 = new Transaction(1001)
  
  graph.addVertex(pa1)
  graph.addVertex(pa2)
  graph.addVertex(pa3)
  graph.addVertex(tr1)
  
  graph.addEdge(pa1.id, new StateForwarderEdge(tr1.id))
  graph.addEdge(pa2.id, new StateForwarderEdge(tr1.id))
  graph.addEdge(tr1.id, new StateForwarderEdge(pa3.id))

  graph.awaitIdle
  val stats = graph.execute(ExecutionConfiguration.withExecutionMode(ExecutionMode.Interactive))
  println(stats)

  graph.foreachVertex(println(_))
  graph.shutdown
}