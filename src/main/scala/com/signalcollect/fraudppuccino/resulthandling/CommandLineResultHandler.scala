package com.signalcollect.fraudppuccino.resulthandling

/**
 * Prints all reported results to the default output
 */
object CommandLineResultHandler extends ComponentResultHandler {

  def processResult(jsonData: String): Unit = {
    println(jsonData)
  }

}