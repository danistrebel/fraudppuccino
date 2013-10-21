package com.signalcollect.fraudppuccino.querylanguage

import FRAUDPPUCCINO._

object StreamingExecutionDemo extends App {
  
  val windowsize = 86400 //1 day
  val maxTimeBetweenConnectedTransactions = 604800 // 1 Week

  val startUnixTime = 1231469665
  val endUnixTime = 1376839940

  for (lower <- startUnixTime to endUnixTime by windowsize) {

    val start = System.currentTimeMillis

    //Retire all transactions that are not relevant anymore
    RETIRE(lower - maxTimeBetweenConnectedTransactions)

    // Loading the transactions of the next window Step
    val loadingStart = System.currentTimeMillis
    LOAD SOURCE args(0) FROM lower TO lower + windowsize
    val loadingTime = (System.currentTimeMillis - loadingStart)

    //Run the matching of incoming and outgoing transactions
    val matchingStart = System.currentTimeMillis
    execution.graph.recalculateScores
    execution.graph.execute
    val matchingTime = (System.currentTimeMillis - matchingStart)

    MKCOMPONENTS

    LABEL TRANSACTIONS "depth" WITH DEPTH_EXPLORATION

    print(lower + "," + (lower + windowsize))
    print("," + (System.currentTimeMillis() - start))
    print("," + loadingTime)
    print("," + matchingTime)
    print("," + TRANSACTIONS.size)
    println("," + COMPONENTS.size)
  }
}