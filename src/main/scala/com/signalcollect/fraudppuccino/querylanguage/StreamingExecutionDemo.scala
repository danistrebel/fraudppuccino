package com.signalcollect.fraudppuccino.querylanguage

import FRAUDPPUCCINO._

object StreamingExecutionDemo extends App {
  val executionPlan = """
    
  source: """ + args(0) + """
  start: 1231469665
  end: 1376839940
  
  window: 1d
  transactionInterval: 1w
  
  filters: [SIZE > 5, SINKS = 1, SINKVALUE > 10000000000, DEPTH > 3, COUNTRYHOPS > 2]
  
  handlers: [WEBSERVER, CONSOLE]
  
  debug: []
  
  """

  execute(executionPlan)
}