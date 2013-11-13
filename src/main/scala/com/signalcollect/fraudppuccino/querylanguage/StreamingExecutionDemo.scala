package com.signalcollect.fraudppuccino.querylanguage

import FRAUDPPUCCINO._

object StreamingExecutionDemo extends App {
  val executionPlan = """
  
#path of the input data
source: """ + args(0) + """

#tells the parser about the semantics of the entries in the
#input data
parse: 
  id: [0, Int]
  src: [2, Int]
  target: [3, Int]
  value: [4, Long]
  time: [5, Long]

#The sections of the input data that should be processed
start: 01/09/2009 02:50:00
end: 18/08/2013 17:30:00

window: 1d
transactionInterval: 1w

#set to true if the matcher should follow all possible matching possibilites
exhaustiveMatching: true

#conditions that a component has to fulfil to be reported
filters: [SIZE > 5, SIZE<1000, SINKVALUE > 10000000000, DEPTH > 3, COUNTRYHOPS > 2]

#handlers that receive the reported components
#e.g. WEBSERVER, CONSOLE, MONGODB
handlers: [WEBSERVER]

debug: []
  
  """

  execute(executionPlan)
}

//filters: [SIZE > 5, SINKVALUE > 10000000000, DEPTH > 3, COUNTRYHOPS > 2]
