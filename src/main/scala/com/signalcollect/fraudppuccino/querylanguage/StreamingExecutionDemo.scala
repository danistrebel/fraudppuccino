package com.signalcollect.fraudppuccino.querylanguage

import FRAUDPPUCCINO._

object StreamingExecutionDemo extends App {
  val executionPlan = """
  SOURCE """ + args(0) + """
  
  START 1231469665
  END 1376839940
  
  STREAM WINDOW 1d
  TX INTERVAL 1w
  
  FILTER
  SIZE > 10
  DEPTH > 5
 
  RESULTS
  WEBSERVER
  CONSOLE
  
  DEBUG
  WINDOWSTATS
  """

  execute(executionPlan)

  //  var execution = StreamingExecution()
  //  execution = execution SOURCE args(0) START 1231469665 END 1376839940 WINDOWSIZE 86400
  //  execution = execution TXINTERVAL 604800 FILTER Array("SIZE > 10", "DEPTH> 5")
  //  execution execute
}