package com.signalcollect.fraudppuccino.querylanguage

import scala.io.Source
import com.signalcollect.fraudppuccino.repeatedanalysis._
import com.signalcollect._
import com.signalcollect.fraudppuccino.structuredetection.TransactionEdge
import scala.collection.mutable.ArrayBuffer
import language.dynamics
import com.signalcollect.fraudppuccino.evaluation.btc.BTCTransactionMatcher
import com.signalcollect.fraudppuccino.structuredetection.TransactionAnnouncer
import com.signalcollect.fraudppuccino.structuredetection.DownstreamTransactionPatternEdge
import com.signalcollect.fraudppuccino.structuredetection.UpstreamTransactionPatternEdge
import scala.collection.mutable.HashMap
import com.signalcollect.fraudppuccino.visualization.FraudppuchinoServer

class QueryExecution {

  val transactions = HashMap[Int, RepeatedAnalysisVertex[Int]]()
  var components: HashMap[Int, Iterable[RepeatedAnalysisVertex[Int]]] = HashMap[Int, Iterable[RepeatedAnalysisVertex[Int]]]()

  val graph = GraphBuilder.withStorageFactory(factory.storage.JavaMapStorage).build
  var iter: Iterator[String] = null

  val visualizationServer = FraudppuchinoServer()

  /**
   * Loads a new window of transactions.
   * Assumes that the transactions are ordered by time in the input file.
   *
   * @param filePath The file from where the transactions should be loaded
   * @param startTime The min time from where transactions should be loaded
   * @param endTime The max time up to where transactions should be loaded
   *
   */
  def load(
    filePath: String,
    startTime: Long,
    endTime: Long) {
    if (iter == null) {
      iter = Source.fromFile(filePath).getLines
    }

    while (iter.hasNext) {

      val splitted = iter.next.split(",")

      if (splitted(5).toLong >= endTime) {
        println(splitted(0))
        return
      }

      if (splitted(5).toLong >= startTime && splitted(2).toInt != splitted(3).toInt) {
        loadTransaction(splitted(0).toInt * -1, splitted(4).toLong, splitted(5).toLong, splitted(2).toInt, splitted(3).toInt)
      }
    }
  }

  /**
   * Removes all transactions that have timedout.
   *
   * @param maxTime The max time stamp for unconnected transactions to survive
   */
  def retire(maxTime: Long) {
    sendPoisonPillToAllOlderThan(maxTime)
    graph.execute

    //remove all unconnected transactions that have timed out
    graph.foreachVertex(v =>
      if (v.state == true) {
        val vertex = v.asInstanceOf[RepeatedAnalysisVertex[Int]]
        if (vertex.outgoingEdges.exists(edge => edge._2 == DownstreamTransactionPatternEdge || edge._2 == UpstreamTransactionPatternEdge)) {
          vertex.removeAlgorithmImplementation
          transactions += ((vertex.id, vertex))
        } else {
          graph.removeVertex(vertex.id)
        }
      })

    //remove components that are entirely out of the window (i.e. the newest member of the component has expired)
    components.foreach(component => {
      val members = component._2
      val maxComponentTime = maxTime - 1123200
      if (members.map(_.getResult("time").get.asInstanceOf[Long]).max < maxComponentTime) {
        if (members.size > 8) {
          visualizationServer.updateResult(component)
        }
        members.foreach(vertex => {
          transactions -= vertex.id
          graph.removeVertex(vertex.id)
        })
      }
    })
  }

  def execute(transactionsAlgorithm: RepeatedAnalysisVertex[_] => VertexAlgorithm) {
    transactions.foreach(tx =>
      if(tx._2==null) {
        println(transactions)
        println(tx)
      } else {
    	  tx._2.setAlgorithmImplementation(transactionsAlgorithm)
      })
    graph.recalculateScores
    graph.execute
  }

  def label(transactionsLabel: Option[String] = None, transactionsAlgorithm: RepeatedAnalysisVertex[_] => VertexAlgorithm) {
    execute(transactionsAlgorithm)
    if (transactionsLabel.isDefined) {
      val label = transactionsLabel.get
      transactions.foreach(tx => tx._2.retainState(label))
    }
  }

  def loadTransaction(transactionId: Int, value: Long, time: Long, srcId: Int, targetId: Int) {
    val transaction = new RepeatedAnalysisVertex(transactionId)
    transaction.storeAttribute("value", value)
    transaction.storeAttribute("time", time)
    transaction.storeAttribute("src", srcId)
    transaction.storeAttribute("target", targetId)
    transaction.setAlgorithmImplementation(v => TransactionAnnouncer(v))

    val sender = new RepeatedAnalysisVertex(srcId)
    sender.setAlgorithmImplementation(v => BTCTransactionMatcher(v))
    val receiver = new RepeatedAnalysisVertex(targetId)
    receiver.setAlgorithmImplementation(v => BTCTransactionMatcher(v))

    graph.addVertex(transaction)
    graph.addVertex(sender)
    graph.addVertex(receiver)
  }

  def sendPoisonPillToAllOlderThan(maxTime: Long) {
    //Send a time stamped poison pill to all vertices    
    graph.foreachVertexWithGraphEditor(graphEditor => vertex =>
      if (vertex.state == false) {
        vertex.deliverSignal(maxTime, None, graphEditor)
      })
  }

  def shutdown = graph.shutdown

}

