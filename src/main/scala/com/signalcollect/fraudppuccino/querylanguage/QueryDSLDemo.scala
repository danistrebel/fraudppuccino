package com.signalcollect.fraudppuccino.querylanguage

import FRAUDPPUCCINO._

object QueryDSLDemo extends App {
  SHOW
  
  LOAD SOURCE "/Volumes/Data/BTC_August2013/user-user-tx.csv" FROM 1000000 TO 1050000

  FILTER TRANSACTIONS "value" GREATERTHAN 10000000l
  
  CONNECT IF ANY_CONNECTION
  LABEL TRANSACTIONS "depth" WITH DEPTH_EXPLORATION

  println("Transactions" + TRANSACTIONS.size)
  println("Components" + COMPONENTS.size)
  
  STORE COMPONENTS "a"
  FILTER COMPONENTS SIZE GREATERTHAN 1
  
  println("bigger than 1:" + COMPONENTS.size)
  
  LOAD COMPONENTS "a"
  FILTER COMPONENTS "depth" MAX VALUE GREATERTHAN 3
  println("deeper than 3:" + COMPONENTS)
  
  SHOW
}