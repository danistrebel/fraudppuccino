package com.signalcollect.fraudppuccino.querylanguage

import com.signalcollect.fraudppuccino.evaluation.btc._
import com.signalcollect.fraudppuccino.repeatedanalysis._
import com.signalcollect.fraudppuccino.structuredetection._
import com.signalcollect.fraudppuccino.patternanalysis._
import scala.collection.mutable.HashMap
import language.dynamics
import com.signalcollect.fraudppuccino.structuredetection.MatchingMode
import com.signalcollect.fraudppuccino.structuredetection.MATCH_CHAIN
import com.signalcollect.fraudppuccino.structuredetection.MATCH_ALL
import com.signalcollect.fraudppuccino.structuredetection.BTCTransactionMatcher
import scala.collection.Iterator

/**
 * DSL to control a fraudppuccino analysis session
 */
object FRAUDPPUCCINO {

  def execute(s: String) {
    var execution = StreamingExecution()
    var lineIter = s.split(System.getProperty("line.separator")).toIterator

    while (lineIter.hasNext) {
      val statement = lineIter.next
      val statementElements = statement.split("\\s+").map(_.trim).filter(_.length > 0)

      if (!statementElements.isEmpty) {
        statementElements(0) match {
          case "SOURCE" => execution = execution.SOURCE(statementElements(1))
          case "START" => execution = execution.START(statementElements(1).toLong)
          case "END" => execution = execution.END(statementElements(1).toLong)
          case "STREAM" => if (statementElements(1) == "WINDOW") execution = execution.WINDOWSIZE(parseSec(statementElements(2)))
          case "TX" => if (statementElements(1) == "INTERVAL") execution = execution.WINDOWSIZE(parseSec(statementElements(2)))
          case "FILTER" => execution = execution.FILTER(readUntilEmptyLine(lineIter))
          case "RESULTS" => execution = execution.RESULTHANDER(readUntilEmptyLine(lineIter))
          case "DEBUG" => execution = execution.DEBUG(readUntilEmptyLine(lineIter))
        }
      }
    }

    execution.execute

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
  
  /**
   * returns all following line until an empty line or the end of the input is reached
   */ 
  def readUntilEmptyLine(iter: Iterator[String]) : List[String] = {
    if(iter.hasNext) {
      val line = iter.next.trim
      if(line.size > 0) {
        println("reading:" + line)
        line :: readUntilEmptyLine(iter)
      } else {
        Nil
      }
    } else {
      Nil
    }
  }
}