package com.signalcollect.fraudppuccino.componentanalysis

import com.signalcollect.fraudppuccino.componentanalysis.algorithms.CountryHopCounter
import com.signalcollect.fraudppuccino.componentanalysis.algorithms.PatternDepthAnalyzer
import com.signalcollect.fraudppuccino.componentanalysis.algorithms.EqualSplits
import com.signalcollect.fraudppuccino.componentanalysis.algorithms.CircleDetection
import com.signalcollect.fraudppuccino.componentanalysis.algorithms.FastSplits

object ComponentMemberAlgorithms {

  /*
   * Queries the component for the max depth i.e. the longest path from any source to a sink transaction
   */
  val depthMemberAlgorithm = ComponentMemberAlgorithm(vertex => new PatternDepthAnalyzer(vertex))

  /*
   * Counts the number of sink accounts 
   */
  val sinkAccountAlgorithm = ComponentMemberQuery(vertex =>
    if (vertex.isPatternSink) ComponentMemberResponse(Some(vertex.targetId)) else ComponentMemberResponse(None))

  /*
   * Counts the number of sink transactions 
   */
  val sinkTransactionAlgorithm = ComponentMemberQuery(vertex =>
    if (vertex.isPatternSink) ComponentMemberResponse(Some(vertex.id)) else ComponentMemberResponse(None))

  /*
   * Counts the number of source accounts 
   */
  val sourceAccountAlgorithm = ComponentMemberQuery(vertex =>
    if (vertex.isPatternSource) ComponentMemberResponse(Some(vertex.targetId)) else ComponentMemberResponse(None))

  /*
   * Counts the number of source transactions 
   */
  val sourceTransactionAlgorithm = ComponentMemberQuery(vertex =>
    if (vertex.isPatternSource) ComponentMemberResponse(Some(vertex.id)) else ComponentMemberResponse(None))

  /*
   * Counts the number source transactions in cash
   */
  val cashSourceMemberAlgorithm = ComponentMemberQuery(vertex =>
    if (vertex.isPatternSource && vertex.isCash) ComponentMemberResponse(Some(vertex.id)) else ComponentMemberResponse(None))

  /**
   * Looks at the value at the sink transactions
   */
  val sinkValueQuery = ComponentMemberQuery(vertex => if (vertex.isPatternSink) ComponentMemberResponse(vertex.getResult("value")) else ComponentMemberResponse(Some(0l)))
  
  /**
   * Looks at the value at the source transactions
   */
  val sourceValueQuery = ComponentMemberQuery(vertex => if (vertex.isPatternSource) ComponentMemberResponse(vertex.getResult("value")) else ComponentMemberResponse(Some(0l)))

  
  /**
   * Looks at the value at the source transactions
   */
  val valueQuery = ComponentMemberQuery(vertex =>ComponentMemberResponse(Some(vertex.value)))
  /*
   * Queries the component for the max country hop count i.e. the max number of cross country transactions from any source to a sink transaction
   */
  val xCountryMemberAlgorithm = ComponentMemberAlgorithm(vertex => new CountryHopCounter(vertex))

  /**
   * Checks if accounts occur repeatedly in a transaction flow
   */
  val circleMemberAlgorithm = ComponentMemberAlgorithm(vertex => new CircleDetection(vertex))

  /**
   * Counts splits where all splits have the same size
   */
  val equalSplits = ComponentMemberAlgorithm(vertex => new EqualSplits(vertex))
  
  /**
   * Counts splits where all splits happened at the same day as the transaction
   */
  val sameDaySplits = ComponentMemberAlgorithm(vertex => new FastSplits(vertex))
}