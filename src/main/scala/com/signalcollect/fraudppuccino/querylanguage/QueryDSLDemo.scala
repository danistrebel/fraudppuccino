package com.signalcollect.fraudppuccino.querylanguage

import FRAUDPPUCCINO._

object QueryDSLDemo extends App {
  SHOW //Just to start the web server

  val windowsize = 200000
  for (lower <- 0 to 50000000 by windowsize) {

	val start = System.currentTimeMillis
    val loadingStart = System.currentTimeMillis

    //Loading the transactions
    LOAD SOURCE args(0) FROM lower TO lower + windowsize
    val loadingTime = (System.currentTimeMillis - loadingStart)

    FILTER TRANSACTIONS "value" GREATERTHAN 10000000l

    //Matching of connected transactions
    val matchingStart = System.currentTimeMillis
    CONNECT IF ANY_CONNECTION
    val matchingTime = (System.currentTimeMillis - matchingStart)

    //    println("INTERVAL " + lower + " - " + (lower + windowsize))
    //    println("Transactions" + TRANSACTIONS.size)
    //    println("Components" + COMPONENTS.size)

    LABEL TRANSACTIONS "depth" WITH DEPTH_EXPLORATION

    //	  STORE COMPONENTS "a"
    //	  FILTER COMPONENTS SIZE GREATERTHAN 1
    //	  
    //	  println("bigger than 1:" + COMPONENTS.size)
    //	  
    //	  LOAD COMPONENTS "a"

    FILTER COMPONENTS "depth" MAX VALUE GREATERTHAN 6
    print(lower + "," + (lower + windowsize))
    print("," + (System.currentTimeMillis() - start))
    print("," + loadingTime)
    print("," + matchingTime)
    print("," + TRANSACTIONS.size)
    println("," + COMPONENTS.size)

    SHOW
  }
      
  SHUTDOWN
}