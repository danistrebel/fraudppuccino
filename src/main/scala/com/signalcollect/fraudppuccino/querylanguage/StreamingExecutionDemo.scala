package com.signalcollect.fraudppuccino.querylanguage

import FRAUDPPUCCINO._

object StreamingExecutionDemo extends App {
  
  val complexity = if(args.size>=2) args(1) else 10
  val txInterval = if(args.size>=3) args(2) else "1w"

  
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
transactionInterval: """ + txInterval + """

#set to true if the matcher should follow all possible matching possibilities
exhaustiveMatching: true

#set the max number of inputs and outputs that are matched against each other
matchingComplexity: """ + complexity + """

#sets the max duration of components to prevent them from lasting for the entire streaming period.
maxComponentDuration: 8w

#conditions that a component has to fulfil to be reported
filters: [SIZE > 5,SIZE < 1000, SINKVALUE > 10000000000, DEPTH > 3, COUNTRYHOPS > 2]

#handlers that receive the reported components
#e.g. WEBSERVER, CONSOLE, MONGODB
handlers: [WEBSERVER, COUNTING]

debug: true
  
  """

  execute(executionPlan)
}

//filters: [SIZE > 5, SINKVALUE > 10000000000, DEPTH > 3, COUNTRYHOPS > 2]
