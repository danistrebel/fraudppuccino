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
    def apply(plan: ExecutionPlan) = execution.execute(plan.transactionsAlgorithm, plan.sendersAlgorithm)
  }

  object LABEL {
    def TRANSACTIONS(label: String) = LabelParser(Some(label), None)
    def SENDERS(label: String) =  LabelParser(None, Some(label))
  }

  case class LabelParser(transactionLabel: Option[String] = None, senderLabel: Option[String] = None) {
    def TRANSACTIONS(label: String) = LabelParser(Some(label), senderLabel)
    def SENDERS(label: String) =  LabelParser(transactionLabel, Some(label))
    def WITH(plan: ExecutionPlan) = execution.label(transactionLabel, senderLabel, plan.transactionsAlgorithm, plan.sendersAlgorithm)
  }

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