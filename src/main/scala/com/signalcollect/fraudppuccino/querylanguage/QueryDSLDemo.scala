package com.signalcollect.fraudppuccino.querylanguage

import FRAUDPPUCCINO._

object QueryDSLDemo extends App {

  LOAD SOURCE "/Volumes/Data/BTC_August2013/user-user-tx.csv" FROM 1000000 TO 1100000

  val WHERE = "MOOH"
  
  FILTER TRANSACTIONS WHERE //apply "value" LESSTHAN 100000000l

  
  CONNECT IF ANY_CONNECTION

  LABEL TRANSACTIONS "depth" WITH DEPTH_EXPLORATION

  val transactionsByComponentId = COMPONENTS

  val connectedComponents = transactionsByComponentId.filter(_._2.size > 1)
  println("Transactions: " + execution.transactions.size)
  println("Components: " + connectedComponents.size)
  println("their depths: " + connectedComponents.map(_._2.map(_.getResult("depth").get.asInstanceOf[Int]).max))
  println("depth larger than 10: " + connectedComponents.map(_._2.map(_.getResult("depth").get.asInstanceOf[Int]).max).filter(_ > 10).size)

  println("Larger than 10: " + connectedComponents.filter(_._2.size > 10).size)
  println("sizes: " + connectedComponents.filter(_._2.size > 10).map(_._2.size))

  println("Unconnected Transactions: " + transactionsByComponentId.filter(_._2.size == 1).size)
}