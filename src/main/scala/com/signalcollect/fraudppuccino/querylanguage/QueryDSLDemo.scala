package com.signalcollect.fraudppuccino.querylanguage

import FRAUDPPUCCINO._

object QueryDSLDemo extends App {
  SHOW //Just to start the web server
  
  val start = System.currentTimeMillis();
  for(lower <- 0 to 50000000 by 200000) {
	  LOAD SOURCE "/Volumes/Data/BTC_August2013/user-user-tx.csv" FROM lower TO lower+200000
	  println("loded " + lower)
	  
	  FILTER TRANSACTIONS "value" GREATERTHAN 10000000l
	  
	  CONNECT IF CHAIN_CONNECTION
	  LABEL TRANSACTIONS "depth" WITH DEPTH_EXPLORATION
	  
	  println("Transactions" + TRANSACTIONS.size)
	  println("Components" + COMPONENTS.size)
	  
//	  STORE COMPONENTS "a"
//	  FILTER COMPONENTS SIZE GREATERTHAN 1
//	  
//	  println("bigger than 1:" + COMPONENTS.size)
//	  
//	  LOAD COMPONENTS "a"
	  FILTER COMPONENTS "depth" MAX VALUE GREATERTHAN 2
	  println("deeper than 2:" + COMPONENTS)
	  
	  SHOW    
  } 
  
  println(System.currentTimeMillis()-start + "ms")
  SHUTDOWN
}