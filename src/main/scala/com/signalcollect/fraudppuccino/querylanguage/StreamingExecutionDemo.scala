package com.signalcollect.fraudppuccino.querylanguage

import FRAUDPPUCCINO._

object StreamingExecutionDemo extends App {
  val executionPlan = """
    
source: """ + args(0) + """
parse:
  id: [0, Int]
  src: [2, Int]
  target: [3, Int]
  value: [4, Long]
  time: [5, Long]

start: 01/09/2009 02:50:00
end: 18/08/2013 17:30:00

window: 1d
transactionInterval: 1w

filters: [SIZE > 5, SINKVALUE > 10000000000, DEPTH > 3, COUNTRYHOPS > 2]

handlers: [WEBSERVER, CONSOLE]

debug: []
  
  """

  execute(executionPlan)
}