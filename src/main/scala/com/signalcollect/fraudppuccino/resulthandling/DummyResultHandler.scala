package com.signalcollect.fraudppuccino.resulthandling

import scala.util.parsing.json.JSON

/**
 * To test messages sent to the result handler
 */
case object DummyResultsHandler extends ComponentResultHandler {

  var reportedComponents: scala.collection.mutable.Map[Int, Int] = scala.collection.mutable.Map()

  def processResult(jsonData: String) {
    val parsed = JSON.parseFull(jsonData).get.asInstanceOf[Map[String, Any]]
    reportedComponents += ((parsed("id").asInstanceOf[Double].intValue, parsed("members").asInstanceOf[List[_]].size))
  }
}