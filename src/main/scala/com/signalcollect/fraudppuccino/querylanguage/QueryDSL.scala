package com.signalcollect.fraudppuccino.querylanguage

import com.signalcollect.fraudppuccino.evaluation.btc._
import com.signalcollect.fraudppuccino.repeatedanalysis._
import com.signalcollect.fraudppuccino.structuredetection._
import com.signalcollect.fraudppuccino.patternanalysis._

import language.dynamics
object FRAUDPPUCCINO  extends App {

  val execution = new QueryExecution
  var components: Map[Any, Iterable[RepeatedAnalysisVertex[_]]] = null

  object LOAD {
    def SOURCE(path: String) = RangeParser(path)
  }

  object CONNECT {
    def IF(condition: ConnectionCondition) = {
      RUN(CONNECTOR) //config missing here i.e. chains / splits / aggregation
      LABEL TRANSACTIONS "component" WITH SUBGRAPH_IDENTIFICATION
      components = execution.transactions.groupBy(_.getResult("component").get.asInstanceOf[Int])

    }
  }

  object RUN {
    def apply(plan: ExecutionPlan) = execution.execute(plan.transactionsAlgorithm, plan.sendersAlgorithm)
  }

  object LABEL {
    def TRANSACTIONS(label: String) = LabelParser(Some(label), None)
    def SENDERS(label: String) = LabelParser(None, Some(label))
  }
  val WHERE = (s: String) => FilterParser(s)

  object FILTER {
    //def TRANSACTIONS(o: Any): String => FilterParser = (s: String) => FilterParser(s) 
    def TRANSACTIONS(s: String) = new Object {
      //def WHERE(s: String) = {}      
    }
  }

//  case class ParseHelper extends Dynamic {
//    def selectDynamic(name: String) = ParseHelper
//    def applyDynamic(name: String, args: Any*) = ParseHelper
//    def apply(as: Any*) = ParseHelper
//  }

  case class Parser(l: List[Any]) extends Dynamic  {
    def applyDynamic(name: String)(args: Any*) = { Parser(args(0)::name::l)}
    
  }
  val entry = Parser(Nil)


  //val a = 1
  val b = 2
  //val c = 3
  val d = 4
  //val e = 5
  val f = 6
  //val g = 7
  val h = 8
  
  val r = entry / b > 1 bello 
  f gooooooooal
  h;
  
  println(r.l.reverse)

  case class FilterParser(val label: String) {
    def EQUALS(referenceValue: Any) = {
      execution.transactions = execution.transactions.filter(tx => tx.getResult(label).get == referenceValue)

    }
    def LESSTHAN(referenceValue: Any) = {
      execution.transactions = execution.transactions.filter(tx => {
        val fieldValue = tx.getResult(label).get
        fieldValue match {
          case field: Int => field < referenceValue.asInstanceOf[Int]
          case field: Long => field < referenceValue.asInstanceOf[Long]
          case field: Float => field < referenceValue.asInstanceOf[Float]
          case field: String => field < referenceValue.asInstanceOf[String]
        }
      })
    }

    def GREATERTHAN(referenceValue: Any) = {
      execution.transactions = execution.transactions.filter(tx => {
        val fieldValue = tx.getResult(label).get
        fieldValue match {
          case field: Int => field > referenceValue.asInstanceOf[Int]
          case field: Long => field > referenceValue.asInstanceOf[Long]
          case field: Float => field > referenceValue.asInstanceOf[Float]
          case field: String => field > referenceValue.asInstanceOf[String]
        }
      })
    }

  }

  def COMPONENTS = components
  def TRANSACTIONS = execution.transactions
  def SENDERS = execution.transactions

  /**
   * PARSING UTILITIES
   */
  case class LabelParser(transactionLabel: Option[String] = None, senderLabel: Option[String] = None) {
    def TRANSACTIONS(label: String) = LabelParser(Some(label), senderLabel)
    def SENDERS(label: String) = LabelParser(transactionLabel, Some(label))
    def WITH(plan: ExecutionPlan) = execution.label(transactionLabel, senderLabel, plan.transactionsAlgorithm, plan.sendersAlgorithm)
  }

  case class RangeParser(path: String = "", start: Int = 0, end: Int = 0) {
    def FROM(i: Int) = this.copy(start = i)
    def TO(i: Int) = execution.load(path, start, i)
  }

  /**
   * CONNECTION CONDITIONS
   *
   * Define how transactions should be connected
   */
  abstract class ConnectionCondition {

  }

  object ANY_CONNECTION extends ConnectionCondition {

  }

  /**
   * EXECUTION PLANS
   *
   * Define algorithms that can be run on the graph structure.
   */
  abstract class ExecutionPlan {
    def transactionsAlgorithm: RepeatedAnalysisVertex[_] => VertexAlgorithm = vertex => new DummyVertexAlgorithm()
    def sendersAlgorithm: RepeatedAnalysisVertex[_] => VertexAlgorithm = vertex => new DummyVertexAlgorithm()
  }

  object CONNECTOR extends ExecutionPlan {
    override def sendersAlgorithm = vertex => new BTCTransactionMatcher(vertex)
    override def transactionsAlgorithm = vertex => new TransactionAnnouncer(vertex)
  }

  object SUBGRAPH_IDENTIFICATION extends ExecutionPlan {
    override def transactionsAlgorithm = vertex => new ConnectedComponentsIdentifier(vertex)
  }

  object DEPTH_EXPLORATION extends ExecutionPlan {
    override def transactionsAlgorithm = vertex => new PatternDepthAnalyzer(vertex)
  }

  object REST { //debug only
    //    val transactionsByComponentId = execution.transactions.groupBy(_.getResult("component").get.asInstanceOf[Int])
    //
    //    val connectedComponents = transactionsByComponentId.filter(_._2.size > 1)
    //    println("Transactions: " + execution.transactions.size)
    //    println("Components: " + connectedComponents.size)
    //    println("their depths: " + connectedComponents.map(_._2.map(_.getResult("depth").get.asInstanceOf[Int]).max))
    //    println("depth larger than 10: " + connectedComponents.map(_._2.map(_.getResult("depth").get.asInstanceOf[Int]).max).filter(_ > 10).size)
    //
    //    println("Larger than 10: " + connectedComponents.filter(_._2.size > 10).size)
    //    println("sizes: " + connectedComponents.filter(_._2.size > 10).map(_._2.size))
    //
    //    println("Unconnected Transactions: " + transactionsByComponentId.filter(_._2.size == 1).size)
  }
}