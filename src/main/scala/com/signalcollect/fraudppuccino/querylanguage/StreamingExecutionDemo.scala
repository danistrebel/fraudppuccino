package com.signalcollect.fraudppuccino.querylanguage

import FRAUDPPUCCINO._

object StreamingExecutionDemo extends App {

  SHOW //Just to start the web server

  val windowsize = 86400 //1 day
  val maxTimeBetweenConnectedTransactions = 604800 // 1 Week

  val startUnixTime = 1231460000
  val endUnixTime = 1269821600 //1376839940

  for (lower <- startUnixTime to endUnixTime by windowsize) {

    val start = System.currentTimeMillis
    val loadingStart = System.currentTimeMillis

    // Loading the transactions of the next window Step and remove all expired transactions
    LOAD SOURCE args(0) FROM lower TO lower + windowsize EXPIRING (lower - maxTimeBetweenConnectedTransactions)
    val loadingTime = (System.currentTimeMillis - loadingStart)

    MKCOMPONENTS

    print(lower + "," + (lower + windowsize))
    print("," + (System.currentTimeMillis() - start))
    print("," + loadingTime)
    print("," + 0)
    print("," + TRANSACTIONS.size)
    println("," + COMPONENTS.size)

  }
  
  LABEL TRANSACTIONS "depth" WITH DEPTH_EXPLORATION
  FILTER COMPONENTS "depth" MAX VALUE GREATERTHAN 6

  SHOW
}