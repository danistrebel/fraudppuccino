package com.signalcollect.fraudppuccino.querylanguage

import scala.beans.BeanProperty
import scala.collection.JavaConversions._
import java.util.HashMap
import java.util.Calendar
import java.util.TimeZone

/**
 * Captures all relevant information for issuing matcher executions.
 * This also serves as the model for parsing the YAML execution definition. 
 */ 
class ExecutionModel {
  @BeanProperty var source: String = null
  @BeanProperty var parse = new HashMap[String, java.util.List[Object]]()
  @BeanProperty var start: String = null
  @BeanProperty var end: String = null
  @BeanProperty var window: String = null
  @BeanProperty var transactionInterval: String = null
  @BeanProperty var exhaustiveMatching = true
  @BeanProperty var matchingComplexity = 10
  @BeanProperty var maxComponentDuration: String = "0"
  @BeanProperty var filters = new java.util.ArrayList[String]()
  @BeanProperty var handlers = new java.util.ArrayList[String]()
  @BeanProperty var debug: Boolean = false
  
  /**
   * Converts the model in an executable execution plan.
   */ 
  def parseExecution: StreamingExecution = {
    StreamingExecution(source, 
        parseUnixDate(start), 
        parseUnixDate(end), parseSec(window), 
        parseSec(transactionInterval), 
        exhaustiveMatching, 
        matchingComplexity,
        MATCH_ALL,
        parseSec(maxComponentDuration),
        filters, 
        handlers, 
        debug, 
        attributeMapper(parse))
  }

  /**
   * Gets the appropriate converter based on the type name.
   */ 
  def attributeMapper(parsed: java.util.Map[String, java.util.List[Object]]): Map[String, (Int, String => Any)] = {
    
    val intParser: String => Any = intValue => intValue.toInt
    val longParser: String => Any = longValue => longValue.toLong
    val doubleParser: String => Any = doubleValue => doubleValue.toDouble
    val floatParser: String => Any = floatValue => floatValue.toFloat
    val stringParser: String => Any = stringValue => stringValue
    val booleanParser: String => Any = booleanValue => if(booleanValue.length==1) booleanValue.toInt!=0 else booleanValue.toBoolean
    val ignoredParser: String => Any = ignoredValue => null

    def getTypeParserForType(typeName: String): String => Any = {
      typeName.toLowerCase match {
        case "ignore" => ignoredParser
        case "int" => intParser
        case "long" => longParser
        case "float" => floatParser
        case "double" => doubleParser
        case "boolean" => booleanParser
        case _ => throw new Exception("can't recognize type " + typeName + " please use a primitive type or the keyword \"ignore\".")
      }
    }
    parsed.toMap.map(parsingEntry => ((parsingEntry._1, (parsingEntry._2(0).asInstanceOf[Int], getTypeParserForType(parsingEntry._2(1).asInstanceOf[String])))))
  }

  //helpers

  /**
   * parses time units and converts them to seconds
   */
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

          case "min" => 60 * unitTime

          case "sec" => unitTime
          case "s" => unitTime

          case null => unitTime

          case _ => throw new Exception("unparsable unit: " + unit)
        }
      }
    }
  }

  /**
   * parses date times and transfers them to unix time stamps
   *
   * accepts either unix time stamps or date times in the format
   *
   * "MM/DD/YYYY" or "MM/DD/YYYY HH:mm:ss" UTC
   */
  def parseUnixDate(s: String): Long = {
    val Date = "\\s*(\\d[\\d]?)/(\\d[\\d]?)/(\\d{4})\\s*".r
    val DateTime = "\\s*(\\d[\\d]?)/(\\d[\\d]?)/(\\d{4})\\s*(\\d{2}):(\\d{2}):(\\d{2})\\s*".r
    s match {
      case Date(month, day, year) => toUnixTimeStamp(month.toInt, day.toInt, year.toInt, 0, 0, 0)
      case DateTime(month, day, year, hour, minute, second) => toUnixTimeStamp(month.toInt, day.toInt, year.toInt, hour.toInt, minute.toInt, second.toInt)
      case _ => s.toInt
    }

  }

  def toUnixTimeStamp(month: Int, day: Int, year: Int, hours: Int, minutes: Int, seconds: Int): Long = {
    val date = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    date.set(year, month - 1, day, hours, minutes, seconds)
    date.getTimeInMillis / 1000
  }
}

