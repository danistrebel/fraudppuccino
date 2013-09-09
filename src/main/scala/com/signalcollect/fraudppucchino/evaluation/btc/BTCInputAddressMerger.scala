package com.signalcollect.fraudppucchino.evaluation.btc

import com.signalcollect._
import scala.io.Source
import com.signalcollect.interfaces.AggregationOperation
import scala.reflect.io.Path
import java.io._

/**
 * Input Address of a Transaction
 */
class BTCInputAddress(id: Int) extends DataGraphVertex(id, id) {

  type Signal = Int

  def collect = Math.min(signals.min, state)

  override def scoreSignal: Double = {
    lastSignalState match {
      case None => 1
      case Some(oldState) => (state - oldState).abs
    }
  }
}

class BTCTransaction(id: Int) extends DataGraphVertex(id, Int.MaxValue) {

  type Signal = Int

  def collect = {
    Math.min(signals.min, state)
  }

  override def scoreSignal: Double = {
    lastSignalState match {
      case None => 1
      case Some(oldState) => (state - oldState).abs
    }
  }
}

object BTCInputAddressMerger extends App {
  val graph = GraphBuilder.build

  //Load Transactions
  loadTransactions(args(0), graph)
  println("done loading transactions")

  loadInputs(args(1), graph)
  println("done loading inputs")

  graph.execute
  println("done merging inputs")

  try {
    val txWriter = new PrintWriter(new File("tx-inuser.csv"))
    val addressWriter = new PrintWriter(new File("address-user.csv"))
    graph.foreachVertex(v => v match {
      case tx: BTCTransaction => txWriter.write(tx.id + "," + tx.state + "\n")
      case address: BTCInputAddress => addressWriter.write(address.id + "," + address.state + "\n")
      case _ =>
    })
    txWriter.close
    addressWriter.close
  } catch {
    case t => println("failed printing output")
  }

  //  val tx2InputUser = graph.aggregate(new GetAllTransactions)
  //  writeTuples(tx2InputUser, "tx-inuser.csv")
  //  println("done writing transactions")
  //
  //  val address2User = graph.aggregate(new GetAllInputAddresses)
  //  writeTuples(address2User, "address-user.csv")
  //  println("done writing addresses")

  graph.shutdown

  def loadTransactions(path: String, graph: Graph[Any, Any]) {
    for (line <- Source.fromFile(path).getLines) {
      val splitted = line.split(",")
      val txid = splitted(0).toInt * -1
      graph.addVertex(new BTCTransaction(txid))
    }
  }

  def loadInputs(path: String, graph: Graph[Any, Any]) {
    for (line <- Source.fromFile(path).getLines) {
      val splitted = line.split(",")
      val txid = splitted(0).toInt * -1
      val input_publicKeyId = try {
        splitted(1).toInt
      } catch {
        case t: Throwable => 0 //Because the id of freshly minted bitcoins is NULL
      }
      graph.addVertex(new BTCInputAddress(input_publicKeyId))
      graph.addEdge(txid, new StateForwarderEdge(input_publicKeyId))
      graph.addEdge(input_publicKeyId, new StateForwarderEdge(txid))
    }

  }

  class GetAllTransactions extends AggregationOperation[Iterable[(Int, Int)]] {
    val neutralElement: Iterable[(Int, Int)] = List()
    def extract(v: Vertex[_, _]): Iterable[(Int, Int)] = {
      v match {
        case tx: BTCTransaction => List((tx.id, tx.state))
        case _ => List()
      }
    }
    def reduce(elements: Stream[Iterable[(Int, Int)]]): Iterable[(Int, Int)] = elements.flatten // use foldRight with cons ?
    def aggregate(a: Iterable[(Int, Int)], b: Iterable[(Int, Int)]): Iterable[(Int, Int)] = a ++ b

  }

  class GetAllInputAddresses extends AggregationOperation[List[(Int, Int)]] {
    val neutralElement: List[(Int, Int)] = List()
    def extract(v: Vertex[_, _]): List[(Int, Int)] = {
      v match {
        case tx: BTCInputAddress => List((tx.id, tx.state))
        case _ => List()
      }
    }
    def reduce(elements: Stream[List[(Int, Int)]]): List[(Int, Int)] = elements.foldLeft(neutralElement)(aggregate) // use foldRight with cons ?
    def aggregate(a: List[(Int, Int)], b: List[(Int, Int)]): List[(Int, Int)] = a ++ b

  }

  def writeTuples(pairs: Iterable[(Int, Int)], outFilePath: String) {
    val writer = Path(outFilePath).toFile.writer
    for (pair <- pairs) {
      writer.write(pair._1 + "," + pair._2 + "\n")
    }
    writer.close
  }
}

