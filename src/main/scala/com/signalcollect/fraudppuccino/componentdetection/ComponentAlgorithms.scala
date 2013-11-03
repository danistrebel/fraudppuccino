package com.signalcollect.fraudppuccino.componentdetection

import com.signalcollect.fraudppuccino.patternanalysis.PatternDepthAnalyzer
import com.signalcollect.GraphEditor
import scala.collection.mutable.ArrayBuffer
import com.signalcollect.fraudppuccino.patternanalysis.CountryHopCounter

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
  val sinkMemberAlgorithm = ComponentMemberQuery(vertex => if (vertex.isPatternSink) ComponentMemberResponse(Some(1)) else ComponentMemberResponse(Some(0)))
  val sinkCountAggregator: (Iterable[ComponentMemberMessage], ComponentMaster) => Any = {
    (repliesFromMembers, master) =>
      {
        val replies = repliesFromMembers.asInstanceOf[ArrayBuffer[ComponentMemberResponse]]
        replies.map(_.response.getOrElse(0).asInstanceOf[Int]).sum
      }
  }
  val SinkCounter = ComponentMemberQueryExecution(sinkMemberAlgorithm, sinkCountAggregator)
  
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

}