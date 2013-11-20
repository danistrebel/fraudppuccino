package com.signalcollect.fraudppuccino.componentdetection

import com.signalcollect.GraphEditor
import ComponentMemberResultAggregators._
import ComponentMemberAlgorithms._

/**
 * Provides a number of predefined component algorithms that are used
 * to prune the reported components.
 */
object ComponentAlgorithms {

  /*
   * Queries the master for the size of its component
   */
  val SizeQuery = ComponentMasterQuery(master => master.members.size)

  /**
   * Returns the max depth of any component in the graph.
   */
  val DepthAlgorithm = ComponentAlgorithmExecution(depthMemberAlgorithm, maxInt)

  /**
   * Counts the number of sink accounts in the graph. I.e. accounts where sink transactions are credited.
   */
  val SinkAccountCounter = ComponentMemberQueryExecution(sinkAccountAlgorithm, countDistinctMemberAnswers)

  /**
   * Counts the number of sink transactions in the graph.
   */
  val SinkTransactionCounter = ComponentMemberQueryExecution(sinkTransactionAlgorithm, countMemberAnswers)
  
  /**
   * Counts the number of source accounts in the graph. I.e. accounts where source transactions are originating.
   */
  val SourceAccountCounter = ComponentMemberQueryExecution(sourceAccountAlgorithm, countDistinctMemberAnswers)

  /**
   * Counts the number of source transactions in the graph.
   */
  val SourceTransactionCounter = ComponentMemberQueryExecution(sourceTransactionAlgorithm, countMemberAnswers)

  /**
   * Counts the number of source transactions that were executed in cash
   */
  val CashSourceCounter = ComponentMemberQueryExecution(cashSourceMemberAlgorithm, countMemberAnswers)

  /**
   * Sums up the value of all sink transactions
   */
  val SinkValue = ComponentMemberQueryExecution(sinkValueQuery, longSumAggregator)
  
  /**
   * Sums up the value of all source transactions
   */
  val SourceValue = ComponentMemberQueryExecution(sourceValueQuery, longSumAggregator)
  
  /**
   * Returns the max value of all transactions
   */
  val MaxValue = ComponentMemberQueryExecution(valueQuery, maxLong)

  /**
   * Returns  the max country hop count i.e. the max number of cross country
   * transactions from any source to a sink transaction
   */
  val XCountryHops = ComponentAlgorithmExecution(xCountryMemberAlgorithm, maxInt)

  /**
   * Returns the number of accounts that occur repeatedly in a transaction flow
   */
  val CircleAlgorithm = ComponentAlgorithmExecution(circleMemberAlgorithm, countTrueResponses)

  /**
   * Returns the number of splits where all splits have approximately the same size.
   */
  val FairSplitCounter = ComponentAlgorithmExecution(equalSplits, countTrueResponses)


}