package com.signalcollect.fraudppuccino.componentdetection

object CommandLineResultHandler extends ComponentResultHandler {

  def processResult(jsonData: String): Unit = {
    println(jsonData)
  }

}