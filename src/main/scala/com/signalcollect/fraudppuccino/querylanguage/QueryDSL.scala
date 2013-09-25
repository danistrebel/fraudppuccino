package com.signalcollect.fraudppuccino.querylanguage

import com.signalcollect.fraudppuccino.evaluation.btc._
import com.signalcollect.fraudppuccino.repeatedanalysis._
import com.signalcollect.fraudppuccino.structuredetection._
import com.signalcollect.fraudppuccino.patternanalysis._

object FRAUDPPUCCINO {

  val execution = new QueryExecution

  object LOAD {
    def apply(path: String) = RangeParser(path)
  }

  case class RangeParser(path: String = "", start: Int = 0, end: Int = 0) {
    def FROM(i: Int) = this.copy(start = i)
    def TO(i: Int) = execution.load(path, start, i)
  }

  object RUN {
    def apply(plan: ExecutionPlan) = plan.execute
  }

  object LABEL {

  }

  abstract class ExecutionPlan {
    def execute
  }

  object CONNECTOR extends ExecutionPlan {

    val transactionMatching: RepeatedAnalysisVertex[_] => VertexAlgorithm = vertex => new BTCTransactionMatcher(vertex)
    val transactionAnnouncing: RepeatedAnalysisVertex[_] => VertexAlgorithm = vertex => new TransactionAnnouncer(vertex)

    def execute = {
      execution.execute(transactionAnnouncing, transactionMatching)
    }
  }

  object REST { //degug only

    val dummyAlgorithm: RepeatedAnalysisVertex[_] => VertexAlgorithm = vertex => new DummyVertexAlgorithm()
    val subgraphIdentification: RepeatedAnalysisVertex[_] => VertexAlgorithm = vertex => new ConnectedComponentsIdentifier(vertex)
    val depthExplorer: RepeatedAnalysisVertex[_] => VertexAlgorithm = vertex => new PatternDepthAnalyzer(vertex)

    execution.label(Some("component"), None, subgraphIdentification, dummyAlgorithm)
    execution.label(Some("depth"), None, depthExplorer, dummyAlgorithm)
    val transactionsByComponentId = execution.transactions.groupBy(_.getResult("component").get.asInstanceOf[Int])

    val connectedComponents = transactionsByComponentId.filter(_._2.size > 1)
    println("Transactions: " + execution.transactions.size)
    println("Components: " + connectedComponents.size)
    println("their depths: " + connectedComponents.map(_._2.map(_.getResult("depth").get.asInstanceOf[Int]).max))
    println("depth larger than 10: " + connectedComponents.map(_._2.map(_.getResult("depth").get.asInstanceOf[Int]).max).filter(_ > 10).size)

    println("Larger than 10: " + connectedComponents.filter(_._2.size > 10).size)
    println("sizes: " + connectedComponents.filter(_._2.size > 10).map(_._2.size))

    println("Unconnected Transactions: " + transactionsByComponentId.filter(_._2.size == 1).size)
  }
}