package com.signalcollect.fraudppuccino.componentdetection

import com.signalcollect.fraudppuccino.patternanalysis.PatternDepthAnalyzer
import com.signalcollect.GraphEditor
import scala.collection.mutable.ArrayBuffer
import com.signalcollect.fraudppuccino.patternanalysis.CountryHopCounter
import com.signalcollect.fraudppuccino.patternanalysis.CircleDetection

/**
 * Provides a number of predefined component algorithms that are used
 * to prune the reported components.
 */
object ComponentAlgorithms {

  /*
   * Queries the master for the size of its component
   */
  val SizeQuery = ComponentMasterQuery(master => master.members.size)

  /*
   * Queries the component for the max depth i.e. the longest path from any source to a sink transaction
   */
  val depthMemberAlgorithm = ComponentMemberAlgorithm(vertex => new PatternDepthAnalyzer(vertex))

  //just gets the max returned integer from all component members
  val maxReplyAggregator: (Iterable[ComponentMemberMessage], ComponentMaster) => Any = {
    (repliesFromMembers, master) =>
      {
        val replies = repliesFromMembers.asInstanceOf[ArrayBuffer[ComponentMemberResponse]]
        replies.map(_.response.getOrElse(0).asInstanceOf[Int]).max
      }
  }

  val DepthAlgorithm = ComponentAlgorithmExecution(depthMemberAlgorithm, maxReplyAggregator)

  /*
   * Counts the number of sink accounts 
   */
  val sinkMemberAlgorithm = ComponentMemberQuery(vertex =>
    if (vertex.isPatternSink) ComponentMemberResponse(Some(vertex.targetId)) else ComponentMemberResponse(None))
  val countDistinctMemberAnswers: (Iterable[ComponentMemberMessage], ComponentMaster) => Any = {
    (repliesFromMembers, master) =>
      {
        val replies = repliesFromMembers.asInstanceOf[ArrayBuffer[ComponentMemberResponse]]
        replies.flatMap(_.response).toList.distinct.size
      }
  }
  val SinkCounter = ComponentMemberQueryExecution(sinkMemberAlgorithm, countDistinctMemberAnswers)

  /*
   * Counts the number of source accounts 
   */
  val sourceMemberAlgorithm = ComponentMemberQuery(vertex =>
    if (vertex.isPatternSource) ComponentMemberResponse(Some(vertex.targetId)) else ComponentMemberResponse(None))
  val SourceCounter = ComponentMemberQueryExecution(sourceMemberAlgorithm, countDistinctMemberAnswers)

  /*
   * Counts the number source transactions in cash
   */
  val cashSourceMemberAlgorithm = ComponentMemberQuery(vertex =>
    if (vertex.isPatternSource && vertex.isCash) ComponentMemberResponse(Some(vertex.targetId)) else ComponentMemberResponse(None))
  val countMemberAnswers: (Iterable[ComponentMemberMessage], ComponentMaster) => Any = {
    (repliesFromMembers, master) =>
      {
        val replies = repliesFromMembers.asInstanceOf[ArrayBuffer[ComponentMemberResponse]]
        replies.flatMap(_.response).size
      }
  }
  val CashSourceCounter = ComponentMemberQueryExecution(sourceMemberAlgorithm, countDistinctMemberAnswers)

  /**
   * Looks at the value at the sink transactions
   */
  val sinkValueQuery = ComponentMemberQuery(vertex => if (vertex.isPatternSink) ComponentMemberResponse(vertex.getResult("value")) else ComponentMemberResponse(Some(0l)))

  //sums up all the longs returned by the component members
  val longSumAggregator: (Iterable[ComponentMemberMessage], ComponentMaster) => Any = {
    (repliesFromMembers, master) =>
      {
        val replies = repliesFromMembers.asInstanceOf[ArrayBuffer[ComponentMemberResponse]]
        replies.map(_.response.getOrElse(0l).asInstanceOf[Long]).sum
      }
  }
  val SinkValue = ComponentMemberQueryExecution(sinkValueQuery, longSumAggregator)

  /*
   * Queries the component for the max country hop count i.e. the max number of cross country transactions from any source to a sink transaction
   */
  val xCountryMemberAlgorithm = ComponentMemberAlgorithm(vertex => new CountryHopCounter(vertex))
  val XCountryHops = ComponentAlgorithmExecution(xCountryMemberAlgorithm, maxReplyAggregator)

  /**
   * Checks if accounts occur repeatedly in a transaction flow
   */
  val circleMemberAlgorithm = ComponentMemberAlgorithm(vertex => new CircleDetection(vertex))

  /**
   * Counts how many of the members returned true
   */
  val countTrueResponses: (Iterable[ComponentMemberMessage], ComponentMaster) => Any = {
    (repliesFromMembers, master) =>
      {
        val replies = repliesFromMembers.asInstanceOf[ArrayBuffer[ComponentMemberResponse]]
        replies.count(_.response.get.asInstanceOf[Boolean]==true)
      }
  }

  val CircleAlgorithm = ComponentAlgorithmExecution(circleMemberAlgorithm, countTrueResponses)

}