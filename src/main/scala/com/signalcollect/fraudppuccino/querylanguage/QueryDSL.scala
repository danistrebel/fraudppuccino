package com.signalcollect.fraudppuccino.querylanguage

import com.signalcollect.fraudppuccino.evaluation.btc._
import com.signalcollect.fraudppuccino.repeatedanalysis._
import com.signalcollect.fraudppuccino.structuredetection._
import com.signalcollect.fraudppuccino.patternanalysis._
import scala.collection.mutable.HashMap
import language.dynamics
import com.signalcollect.fraudppuccino.structuredetection.MatchingMode
import com.signalcollect.fraudppuccino.structuredetection.MATCH_CHAIN
import com.signalcollect.fraudppuccino.structuredetection.MATCH_ALL
import com.signalcollect.fraudppuccino.structuredetection.BTCTransactionMatcher

/**
 * DSL to control a fraudppuccino analysis session
 */ 
object FRAUDPPUCCINO {

  lazy val execution: QueryExecution = new QueryExecution

  lazy val snapshots = HashMap[String, Map[Int, Iterable[RepeatedAnalysisVertex[_]]]]()

  object LOAD {
    def SOURCE(path: String) = RangeParser(path = path)
  }

  object CONNECT {
    def IF(condition: ConnectionCondition) = {
      if (condition == CHAIN_CONNECTION) {
        RUN(Connector(MATCH_CHAIN))
      } else {
        RUN(Connector(MATCH_ALL))
      }

    }
  }

  def MKCOMPONENTS = {
    execution.components.clear
    execution.components ++= execution.transactions.map(_._2).groupBy(_.getResult("component").get.asInstanceOf[Int])
  }

  def RETIRE(maxTime: Long) = execution.retire(maxTime)

  object RUN {
    def apply(plan: ExecutionPlan) = execution.execute(plan.transactionsAlgorithm)
  }

  object LABEL {
    def TRANSACTIONS(label: String) = LabelParser(Some(label))
  }

  object FILTER {
    def TRANSACTIONS(label: String) = FilterParser(label)
    def COMPONENTS(operation: String): ComponentFilter = {
      operation match {
        case "size" => ComponentSizeFilter
        case label: String => ComponentLabelFilter(label)
      }
    }
  }

  abstract class ComponentFilter {
    def EQUALS(referenceSize: Int): Map[Int, Iterable[RepeatedAnalysisVertex[_]]]
    def LESSTHAN(referenceSize: Int): Map[Int, Iterable[RepeatedAnalysisVertex[_]]]
    def GREATERTHAN(referenceSize: Int): Map[Int, Iterable[RepeatedAnalysisVertex[_]]]
    def MAX(a: Any): ComponentFilter
    def MIN(a: Any): ComponentFilter
  }

  object ComponentSizeFilter extends ComponentFilter {
    def EQUALS(referenceSize: Int): Map[Int, Iterable[RepeatedAnalysisVertex[_]]] = {
      execution.components.filter(_._2.size == referenceSize).toMap
    }
    def LESSTHAN(referenceSize: Int): Map[Int, Iterable[RepeatedAnalysisVertex[_]]] = {
      execution.components.filter(_._2.size < referenceSize).toMap
    }
    def GREATERTHAN(referenceSize: Int): Map[Int, Iterable[RepeatedAnalysisVertex[_]]] = {
      execution.components.filter(_._2.size > referenceSize).toMap
    }
    def MAX(a: Any): ComponentFilter = { null }
    def MIN(a: Any): ComponentFilter = { null }
  }

  lazy val SIZE = "size"

  case class ComponentLabelFilter(label: String, extractionFunction: Iterable[RepeatedAnalysisVertex[_]] => Int = members => 0) extends ComponentFilter {

    def MAX(a: Any) = {
      val extraction: Iterable[RepeatedAnalysisVertex[_]] => Int = members => members.map(_.getResult(label).get.asInstanceOf[Int]).max
      ComponentLabelFilter(label, extraction)
    }

    def MIN(a: Any) = {
      val extraction: Iterable[RepeatedAnalysisVertex[_]] => Int = members => members.map(_.getResult(label).get.asInstanceOf[Int]).min
      ComponentLabelFilter(label, extraction)
    }

    def EQUALS(referenceSize: Int) = {
      execution.components.filter(c => extractionFunction(c._2) == referenceSize).toMap
    }
    def LESSTHAN(referenceSize: Int) = {
      execution.components.filter(c => extractionFunction(c._2) < referenceSize).toMap
    }
    def GREATERTHAN(referenceSize: Int) = {
      execution.components.filter(c => extractionFunction(c._2) > referenceSize).toMap
    }
  }

  lazy val VALUE = ""

  case class FilterParser(val label: String) {
    def EQUALS(referenceValue: Any) = {
      //      execution.transactions = execution.transactions.filter(tx => tx.getResult(label).get == referenceValue)

    }
    def LESSTHAN(referenceValue: Any) = {
      //      execution.transactions = execution.transactions.filter(tx => {
      //        val fieldValue = tx.getResult(label).get
      //        fieldValue match {
      //          case field: Int => field < referenceValue.asInstanceOf[Int]
      //          case field: Long => field < referenceValue.asInstanceOf[Long]
      //          case field: Float => field < referenceValue.asInstanceOf[Float]
      //          case field: String => field < referenceValue.asInstanceOf[String]
      //        }
      //      })
    }

    def GREATERTHAN(referenceValue: Any) = {
      //      execution.transactions = execution.transactions.filter(tx => {
      //        val fieldValue = tx.getResult(label).get
      //        fieldValue match {
      //          case field: Int => field > referenceValue.asInstanceOf[Int]
      //          case field: Long => field > referenceValue.asInstanceOf[Long]
      //          case field: Float => field > referenceValue.asInstanceOf[Float]
      //          case field: String => field > referenceValue.asInstanceOf[String]
      //        }
      //      })
    }

  }

  object SHUTDOWN {
    execution.shutdown
  }

  def COMPONENTS = execution.components
  def TRANSACTIONS = execution.transactions
  def SENDERS = execution.transactions

  /**
   * PARSING UTILITIES
   */
  case class LabelParser(transactionLabel: Option[String] = None) {
    def TRANSACTIONS(label: String) = LabelParser(Some(label))
    def WITH(plan: ExecutionPlan) = execution.label(transactionLabel, plan.transactionsAlgorithm)
  }

  case class RangeParser(path: String = "", start: Long = 0l, end: Long = 0l) {
    def FROM(i: Long) = this.copy(start = i)
    def TO(i: Long) = {
      execution.load(path, start, i)
    }
  }

  /**
   * CONNECTION CONDITIONS
   *
   * Define how transactions should be connected
   */
  abstract class ConnectionCondition {

  }

  object ANY_CONNECTION extends ConnectionCondition
  object CHAIN_CONNECTION extends ConnectionCondition

  /**
   * EXECUTION PLANS
   *
   * Define algorithms that can be run on the graph structure.
   */
  abstract class ExecutionPlan {
    def transactionsAlgorithm: RepeatedAnalysisVertex[_] => VertexAlgorithm = vertex => new DummyVertexAlgorithm()
    def sendersAlgorithm: RepeatedAnalysisVertex[_] => VertexAlgorithm = vertex => new DummyVertexAlgorithm()
  }

  case class Connector(matchingMode: MatchingMode) extends ExecutionPlan {
    override def sendersAlgorithm = vertex => new BTCTransactionMatcher(vertex, matchingMode)
    override def transactionsAlgorithm = vertex => new TransactionAnnouncer(vertex)
  }

  object DEPTH_EXPLORATION extends ExecutionPlan {
    override def transactionsAlgorithm = vertex => new PatternDepthAnalyzer(vertex)
  }

}