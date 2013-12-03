package com.signalcollect.fraudppuccino.resulthandling

trait ComponentResultHandler {
  def processResult(jsonData: String): Unit
  def processStatusMessage(jsonStatus: String) = {}
}

case class ComponentResultHandlerFactory(s: String) {
  def apply(): ComponentResultHandler = {
    s.toUpperCase match {
      case "CONSOLE" => CommandLineResultHandler
      case "WEBSERVER" => FraudppuccinoServer
      case "MONGODB"=> MongoDBResultHandler
      case "COUNTING" => CountingResultHandler
      case "DUMMY" => DummyResultsHandler
    }
  }
}
