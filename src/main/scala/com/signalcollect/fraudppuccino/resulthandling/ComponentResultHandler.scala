package com.signalcollect.fraudppuccino.resulthandling

abstract class ComponentResultHandler {
  def processResult(jsonData: String): Unit
  def processStatusMessage(jsonStatus: String) = {}
}

object ComponentResultHandler {
  def apply(s: String): ComponentResultHandler = {
    s match {
      case "CONSOLE" => CommandLineResultHandler
      case "WEBSERVER" => FraudppuccinoServer
      case "MONGODB"=> MongoDBResultHandler
    }
  }
}
