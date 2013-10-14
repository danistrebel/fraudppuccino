package com.signalcollect.fraudppuccino.querylanguage

import FRAUDPPUCCINO._

object StreamingExecutionDemo extends App {

  val windowsize = 86400 //1 day
  val maxTimeBetweenConnectedTransactions = 604800 // 1 Week

  val startUnixTime = 1231460000
  val endUnixTime = 1376839940

  for (lower <- startUnixTime to endUnixTime by windowsize) {

    val start = System.currentTimeMillis

    //Retire all transactions that are not relevant anymore
    RETIRE(lower - maxTimeBetweenConnectedTransactions)

    val loadingStart = System.currentTimeMillis

    // Loading the transactions of the next window Step
    LOAD SOURCE args(0) FROM lower TO lower + windowsize

    val loadingTime = (System.currentTimeMillis - loadingStart)

    MKCOMPONENTS
    LABEL TRANSACTIONS "depth" WITH DEPTH_EXPLORATION


    print(lower + "," + (lower + windowsize))
    print("," + (System.currentTimeMillis() - start))
    print("," + loadingTime)
    print("," + 0)
    print("," + TRANSACTIONS.size)
    println("," + COMPONENTS.size)

  }
}