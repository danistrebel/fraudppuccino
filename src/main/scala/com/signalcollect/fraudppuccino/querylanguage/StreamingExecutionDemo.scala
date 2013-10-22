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
  SIZE > 10
  DEPTH > 5
 
  RESULTS
  WEBSERVER
  CONSOLE
  
  DEBUG
  WINDOWSTATS
  """

  execute(executionPlan)
}