package com.signalcollect.fraudppuccino.resulthandling

object CountingResultHandler extends ComponentResultHandler {
  var count = 0

  def processResult(jsonData: String): Unit = {
    count+=1
  }
  
  override def processStatusMessage(jsonStatus: String): Unit = {
    print(count + ",")
  }

}