package com.signalcollect.fraudppuccino.querylanguage

import FRAUDPPUCCINO._

object QueryDSLDemo extends App {
  SHOW //Just to start the web server
  for(lower <- 1000000 to 1200000 by 100000) {
	  LOAD SOURCE "/Volumes/Data/BTC_August2013/user-user-tx.csv" FROM lower TO lower+100000
	  
	  FILTER TRANSACTIONS "value" GREATERTHAN 10000000l
	  
	  CONNECT IF ANY_CONNECTION
	  LABEL TRANSACTIONS "depth" WITH DEPTH_EXPLORATION
	  
	  println("Transactions" + TRANSACTIONS.size)
	  println("Components" + COMPONENTS.size)
	  
	  STORE COMPONENTS "a"
	  FILTER COMPONENTS SIZE GREATERTHAN 1
	  
	  println("bigger than 1:" + COMPONENTS.size)
	  
	  LOAD COMPONENTS "a"
	  FILTER COMPONENTS "depth" MAX VALUE GREATERTHAN 6
	  println("deeper than 6:" + COMPONENTS)
	  
	  SHOW    
  } 
  SHUTDOWN
}