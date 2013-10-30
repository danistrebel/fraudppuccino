package com.signalcollect.fraudppuccino.querylanguage

import scala.beans.BeanProperty
import scala.collection.JavaConversions._ 

class ExecutionModel {
  @BeanProperty var source: String = null
  @BeanProperty var start: String = null
  @BeanProperty var end: String = null
  @BeanProperty var window: String = null
  @BeanProperty var transactionInterval: String = null
  @BeanProperty var filters = new java.util.ArrayList[String]()
  @BeanProperty var handlers = new java.util.ArrayList[String]()
  @BeanProperty var debug = new java.util.ArrayList[String]()
  
  def parseExecution: StreamingExecution = {
    StreamingExecution(source, start.toLong, end.toLong, parseSec(window), parseSec(transactionInterval), filters, handlers, debug)
  }
  
  //helpers

  def parseSec(s: String): Long = {
    val TimeWithUnit = "\\s*([\\d]+)\\s*([\\w]+)?\\s*".r
    s match {
      case TimeWithUnit(time, unit) => {
        val unitTime = time.toLong
        unit match {
          case "w" => 604800 * unitTime
          case "week" => 604800 * unitTime

          case "d" => 86400 * unitTime
          case "day" => 86400 * unitTime

          case "h" => 3600 * unitTime
          case "hour" => 3600 * unitTime
          
          case "min" => 60* unitTime
          
          case "sec" => unitTime
          case "s" => unitTime
          
          case null => unitTime
          
          case _ => throw new Exception("unparsable unit: " + unit)
        }
      }
    }
  }
}

