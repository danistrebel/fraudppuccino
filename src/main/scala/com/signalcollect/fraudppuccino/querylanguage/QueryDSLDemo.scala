package com.signalcollect.fraudppuccino.querylanguage

import FRAUDPPUCCINO._

object QueryDSLDemo extends App {
  LOAD ("/Volumes/Data/BTC_August2013/user-user-tx.csv") FROM 1000000 TO 1100000
  println("done loading")
  RUN (CONNECTOR) //config missing here
  println("done connecting")
  REST
//	RUN CONNECTOR WITH ALL //CSA
//	LABEL SUBGRAPHS AS "component"
//	LABEL DEPTH as "depth"  
}