package com.signalcollect.fraudppuccino.resulthandling

abstract class ComponentResultHandler {
  def processResult(jsonData: String): Unit
}

object ComponentResultHandler {
  def apply(s: String): ComponentResultHandler = {
    s match {
      case "CONSOLE" => CommandLineResultHandler
      case "WEBSERVER" => new FraudppuccinoServer
    }
  }
}