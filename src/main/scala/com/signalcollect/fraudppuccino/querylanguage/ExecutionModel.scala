package com.signalcollect.fraudppuccino.querylanguage

import scala.beans.BeanProperty
import scala.collection.JavaConversions._
import java.util.HashMap

class ExecutionModel {
  @BeanProperty var source: String = null
  @BeanProperty var parse = new HashMap[String, java.util.List[Object]]()
  @BeanProperty var start: String = null
  @BeanProperty var end: String = null
  @BeanProperty var window: String = null
  @BeanProperty var transactionInterval: String = null
  @BeanProperty var filters = new java.util.ArrayList[String]()
  @BeanProperty var handlers = new java.util.ArrayList[String]()
  @BeanProperty var debug = new java.util.ArrayList[String]()

  def parseExecution: StreamingExecution = {
    StreamingExecution(source, start.toLong, end.toLong, parseSec(window), parseSec(transactionInterval), filters, handlers, debug, attributeMapper(parse))
  }

  def attributeMapper(parsed: java.util.Map[String, java.util.List[Object]]): Map[String, (Int, String => Any)] = {

    val intParser: String => Any = intValue => intValue.toInt
    val longParser: String => Any = longValue => longValue.toLong
    val doubleParser: String => Any = doubleValue => doubleValue.toDouble
    val floatParser: String => Any = floatValue => floatValue.toFloat
    val stringParser: String => Any = stringValue => stringValue
    val ignoredParser: String => Any = ignoredValue => null

    def getTypeParserForType(typeName: String): String => Any = {
      typeName.toLowerCase match {
        case "ignore" => ignoredParser
        case "int" => intParser
        case "long" => longParser
        case "float" => floatParser
        case "double" => doubleParser
        case _ => throw new Exception("can't recognize type " + typeName + " please use a primitive type or the keyword \"ignore\".")
      }
    }    
    parsed.toMap.map(parsingEntry => ((parsingEntry._1.toLowerCase, (parsingEntry._2(0).asInstanceOf[Int], getTypeParserForType(parsingEntry._2(1).asInstanceOf[String])))))
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

          case "min" => 60 * unitTime

          case "sec" => unitTime
          case "s" => unitTime

          case null => unitTime

          case _ => throw new Exception("unparsable unit: " + unit)
        }
      }
    }
  }
}

