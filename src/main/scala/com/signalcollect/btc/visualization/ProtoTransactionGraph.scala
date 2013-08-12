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
import java.io.File
import com.signalcollect.btc.transactiongraph.JsonElement
import scala.io.Source
import com.signalcollect.btc.transactiongraph.OutputAddressForwarderEdge

object ProtoTransactionGraph extends App {

  //  val graphConfig = new GraphConfiguration()
  //  val console = new ConsoleServer(graphConfig)

  val graph = GraphBuilder.build

  // Parse JSON until we have the full transaction data
  val file = new File("/Users/strebel/Google Drive/Uni/Masterthesis/bitcoin")
  val inputs = file.listFiles().filter(_.getName().endsWith(".json"))

  for (input <- inputs) {
    val path = input.getAbsolutePath()
    println(path)
    val data = JsonElement.parse(Source.fromFile(path) mkString).get
    val transaction = data.tx.foreach(t => {
      val transactionOuts = t.out.map(_.scriptPubKey.asString)
      val transactionOutAddresses: List[String] = transactionOuts.map(out => try {out.split(" ")(2)} catch { case ex: Exception => "unknown address format"})
  
      val thisTransaction = new Transaction(t.hash.asString, transactionOutAddresses)
      graph.addVertex(thisTransaction)
      
      for (transactionInput <- t.in) {
        val inputIndex: Int = try { transactionInput.prev_out.n.asInt } catch { case ex: Exception => -1 }
        if (inputIndex >= 0) {
          val inputTransaction = new Transaction(transactionInput.prev_out.hash.asString)
          graph.addVertex(inputTransaction)
          graph.addEdge(inputTransaction.id, new OutputAddressForwarderEdge(thisTransaction.id, inputIndex))
        }

      }
    })

  }

  //  val pa1 = new PublicAddress(1)
  //  val pa2 = new PublicAddress(2)
  //  val pa3 = new PublicAddress(3)
  //
  //  val tr1 = new Transaction(1001)
  //
  //  graph.addVertex(pa1)
  //  graph.addVertex(pa2)
  //  graph.addVertex(pa3)
  //  graph.addVertex(tr1)
  //
  //  graph.addEdge(pa1.id, new StateForwarderEdge(tr1.id))
  //  graph.addEdge(pa2.id, new StateForwarderEdge(tr1.id))
  //  graph.addEdge(tr1.id, new StateForwarderEdge(pa3.id))

  graph.awaitIdle
  val stats = graph.execute
  println(stats)

  graph.foreachVertex(println(_))
  graph.shutdown
}