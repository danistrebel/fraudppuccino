package com.signalcollect.fraudppuccino.querylanguage

import com.signalcollect.fraudppuccino.evaluation.btc._
import com.signalcollect.fraudppuccino.repeatedanalysis._
import com.signalcollect.fraudppuccino.structuredetection._
import com.signalcollect.fraudppuccino.patternanalysis._
import scala.collection.mutable.HashMap
import language.dynamics
import com.signalcollect.fraudppuccino.visualization.FraudppuchinoServer

object FRAUDPPUCCINO {

  lazy val visualizationServer = FraudppuchinoServer()

  lazy val execution: QueryExecution = new QueryExecution
  var components: Map[Int, Iterable[RepeatedAnalysisVertex[_]]] = null

  lazy val snapshots = HashMap[String, Map[Int, Iterable[RepeatedAnalysisVertex[_]]]]()

  object LOAD {
    def SOURCE(path: String) = RangeParser(path=path)
    def COMPONENTS(name: String) = components = snapshots(name)
  }
  
  object STREAM {
    def SOURCE(path: String) = RangeParser(stream = true, path=path)
  }

  object STORE {
    def COMPONENTS(name: String) = if (components != null) {
      snapshots += ((name, components))
    }
  }

  object CONNECT {
    def IF(condition: ConnectionCondition) = {
      if (condition == CHAIN_CONNECTION) {
        RUN(Connector(MATCH_CHAIN))
      } else {
        RUN(Connector(MATCH_ALL))
      }
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
    def EQUALS(referenceSize: Int)
    def LESSTHAN(referenceSize: Int)
    def GREATERTHAN(referenceSize: Int)
    def MAX(a: Any): ComponentFilter
    def MIN(a: Any): ComponentFilter
  }

  object ComponentSizeFilter extends ComponentFilter {
    def EQUALS(referenceSize: Int) = {
      components = components.filter(_._2.size == referenceSize)
    }
    def LESSTHAN(referenceSize: Int) = {
      components = components.filter(_._2.size < referenceSize)
    }
    def GREATERTHAN(referenceSize: Int) = {
      components = components.filter(_._2.size > referenceSize)
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
      components = components.filter(c => extractionFunction(c._2) == referenceSize)
    }
    def LESSTHAN(referenceSize: Int) = {
      components = components.filter(c => extractionFunction(c._2) < referenceSize)
    }
    def GREATERTHAN(referenceSize: Int) = {
      components = components.filter(c => extractionFunction(c._2) > referenceSize)
    }
  }

  lazy val VALUE = ""

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

  object SHUTDOWN {
    execution.shutdown
  }

  def COMPONENTS = components
  def TRANSACTIONS = execution.transactions
  def SENDERS = execution.transactions

  def SHOW = visualizationServer.updateResults(components)

  /**
   * PARSING UTILITIES
   */
  case class LabelParser(transactionLabel: Option[String] = None, senderLabel: Option[String] = None) {
    def TRANSACTIONS(label: String) = LabelParser(Some(label), senderLabel)
    def SENDERS(label: String) = LabelParser(transactionLabel, Some(label))
    def WITH(plan: ExecutionPlan) = execution.label(transactionLabel, senderLabel, plan.transactionsAlgorithm, plan.sendersAlgorithm)
  }

  case class RangeParser(stream: Boolean = false, path: String = "", start: Int = 0, end: Int = 0) {
    def FROM(i: Int) = this.copy(start = i)
    def TO(i: Int) = execution.load(path, start, i, stream)
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

  object SUBGRAPH_IDENTIFICATION extends ExecutionPlan {
    override def transactionsAlgorithm = vertex => new ConnectedComponentsIdentifier(vertex)
  }

  object DEPTH_EXPLORATION extends ExecutionPlan {
    override def transactionsAlgorithm = vertex => new PatternDepthAnalyzer(vertex)
  }

}