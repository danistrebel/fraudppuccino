package com.signalcollect.fraudppuccino.querylanguage

import FRAUDPPUCCINO._

object StreamingExecutionDemo extends App {
  val executionPlan = """
  SOURCE """ + args(0) + """
  
  START 1231469665
  END 1376839940
  
  STREAM WINDOW 1d
  TX INTERVAL 1w
  
  // Filter algorithms must already be registered  
  FILTER
  SIZE > 5
  SINKS = 1
  SINKVALUE > 10000000000
  DEPTH > 3
  COUNTRYHOPS > 2
  
 
  RESULTS
  WEBSERVER
  
  // CONSOLE
  
  DEBUG
  WINDOWSTATS
  """

  execute(executionPlan)
}