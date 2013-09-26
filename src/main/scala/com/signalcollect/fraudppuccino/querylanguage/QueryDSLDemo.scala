package com.signalcollect.fraudppuccino.querylanguage

import FRAUDPPUCCINO._

object QueryDSLDemo extends App {
  LOAD ("/Volumes/Data/BTC_August2013/user-user-tx.csv") FROM 1000000 TO 1100000
  RUN (CONNECTOR) //config missing here
  LABEL TRANSACTIONS "component" WITH SUBGRAPH_IDENTIFICATION
  LABEL TRANSACTIONS "depth" WITH DEPTH_EXPLORATION

  REST

}