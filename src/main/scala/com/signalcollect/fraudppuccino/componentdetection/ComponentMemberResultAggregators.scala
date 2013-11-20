package com.signalcollect.fraudppuccino.componentdetection

import scala.collection.mutable.ArrayBuffer

object ComponentMemberResultAggregators {
  /*
   * Get the max returned integer from all component members
   */
  val maxInt: (Iterable[ComponentMemberMessage], ComponentMaster) => Any = {
    (repliesFromMembers, master) =>
      {
        val replies = repliesFromMembers.asInstanceOf[ArrayBuffer[ComponentMemberResponse]]
        replies.map(_.response.getOrElse(0).asInstanceOf[Int]).max
      }
  }
  
  /*
   * Get the max returned long from all component members
   */
  val maxLong: (Iterable[ComponentMemberMessage], ComponentMaster) => Any = {
    (repliesFromMembers, master) =>
      {
        val replies = repliesFromMembers.asInstanceOf[ArrayBuffer[ComponentMemberResponse]]
        replies.map(_.response.getOrElse(0l).asInstanceOf[Long]).max
      }
  }
  
  /**
   * Counts the number of distinct results returned by the component members
   */
  val countDistinctMemberAnswers: (Iterable[ComponentMemberMessage], ComponentMaster) => Any = {
    (repliesFromMembers, master) =>
      {
        val replies = repliesFromMembers.asInstanceOf[ArrayBuffer[ComponentMemberResponse]]
        replies.flatMap(_.response).toList.distinct.size
      }
  }
  
  /**
   * Counts all the replies that are not None values.
   */
  val countMemberAnswers: (Iterable[ComponentMemberMessage], ComponentMaster) => Any = {
    (repliesFromMembers, master) =>
      {
        val replies = repliesFromMembers.asInstanceOf[ArrayBuffer[ComponentMemberResponse]]
        replies.flatMap(_.response).size
      }
  }
  
  /*
   * Sums up all the longs returned by the component members
   */
  val longSumAggregator: (Iterable[ComponentMemberMessage], ComponentMaster) => Any = {
    (repliesFromMembers, master) =>
      {
        val replies = repliesFromMembers.asInstanceOf[ArrayBuffer[ComponentMemberResponse]]
        replies.map(_.response.getOrElse(0l).asInstanceOf[Long]).sum
      }
  }
  
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

  
}