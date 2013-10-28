package com.signalcollect.fraudppuccino.componentdetection

import com.signalcollect.fraudppuccino.patternanalysis.PatternDepthAnalyzer
import com.signalcollect.GraphEditor
import scala.collection.mutable.ArrayBuffer

object ComponentAlgorithms {

  /*
   * Queries the master for the size of its component
   */
  val SizeQuery = ComponentMasterQuery(master => master.members.size)

  /*
   * Queries the component for the max depth i.e. the longest path from any source to a sink transaction
   */
  val depthMemberAlgorithm = ComponentMemberAlgorithm(vertex => new PatternDepthAnalyzer(vertex))
  val maxDepthAggregator: (Iterable[ComponentMemberMessage], ComponentMaster, GraphEditor[_, _]) => Unit = {
    (repliesFromMembers, master, graphEditor) =>
      {
        val replies = repliesFromMembers.asInstanceOf[ArrayBuffer[ComponentMemberResponse]]
        val maxDepth = replies.map(_.response.getOrElse(0).asInstanceOf[Int]).max
        graphEditor.sendToActor(master.handler, ComponentReply(master.componentId, Some(maxDepth)))
      }
  }

  val DepthAlgorithm = ComponentAlgorithmExecution(depthMemberAlgorithm, maxDepthAggregator)

  /*
   * Counts the number of sink accounts 
   */
  val sinkMemberAlgorithm = ComponentMemberQuery(vertex => if (vertex.isPatternSink) ComponentMemberResponse(Some(1)) else ComponentMemberResponse(Some(0)))
  val sinkCountAggregator: (Iterable[ComponentMemberMessage], ComponentMaster, GraphEditor[_, _]) => Unit = {
    (repliesFromMembers, master, graphEditor) =>
      {
        val replies = repliesFromMembers.asInstanceOf[ArrayBuffer[ComponentMemberResponse]]
        val maxDepth = replies.map(_.response.getOrElse(0).asInstanceOf[Int]).sum
        graphEditor.sendToActor(master.handler, ComponentReply(master.componentId, Some(maxDepth)))
      }
  }
  val SinkCounter = ComponentMemberQueryExecution(sinkMemberAlgorithm, sinkCountAggregator)

}