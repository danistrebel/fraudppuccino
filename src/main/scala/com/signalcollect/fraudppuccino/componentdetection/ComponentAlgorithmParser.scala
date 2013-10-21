package com.signalcollect.fraudppuccino.componentdetection

import scala.collection.mutable.Map

object ComponentAlgorithmParser {

  val sizeQuery = ComponentMasterQuery(master => master.members.size)
  val sizeRetainQuery = ComponentMasterQuery(master => master.members.size, Some("componentSize"))


  val algorithms = Map[String, HandlerRequest]()
  algorithms += (("size", sizeQuery))
  algorithms += (("sizeretain", sizeRetainQuery)) //like "size" but retains the size as a vertex attribute

  def parseWorkFlowStep(s: String): (HandlerRequest, Any => Boolean) = {
    val WorkFlowStep = "([\\w]+)\\s*(<|>|<=|>=|=)\\s*([\\w]+)".r
    try {
      val WorkFlowStep(algorithm, operator, value) = s
      val request = algorithms(algorithm.toLowerCase)
      val comparisonFunction = parseComparisonFunction(operator, value)
      (request, comparisonFunction)
    } catch {
      case t: Throwable => {
        System.err.println("Error while parsing: " + s)
        t.printStackTrace
        throw t
      }
    }
  }

  def parseComparisonFunction(operator: String, value: String): Any => Boolean = {
    operator match {
      case "=" => result: Any => result.toString == value
      case ">" => result: Any => {
        result match {
          case res: Long => res.asInstanceOf[Long] > value.toLong
          case res: Int => res.asInstanceOf[Int] > value.toInt
          case res: Double => res.asInstanceOf[Double] > value.toDouble
          case res: Float => res.asInstanceOf[Float] > value.toFloat
          case _ => false
        }
      }
      case "<" => result: Any => {
        result match {
          case res: Long => res.asInstanceOf[Long] < value.toLong
          case res: Int => res.asInstanceOf[Int] < value.toInt
          case res: Double => res.asInstanceOf[Double] < value.toDouble
          case res: Float => res.asInstanceOf[Float] < value.toFloat
          case _ => false
        }
      }
      case "<=" => result: Any => {
        result match {
          case res: Long => res.asInstanceOf[Long] <= value.toLong
          case res: Int => res.asInstanceOf[Int] <= value.toInt
          case res: Double => res.asInstanceOf[Double] <= value.toDouble
          case res: Float => res.asInstanceOf[Float] <= value.toFloat
          case _ => false
        }
      }
      case ">=" => result: Any => {
    	  result match {
    	  case res: Long => res.asInstanceOf[Long] >= value.toLong
    	  case res: Int => res.asInstanceOf[Int] >= value.toInt
    	  case res: Double => res.asInstanceOf[Double] >= value.toDouble
    	  case res: Float => res.asInstanceOf[Float] >= value.toFloat
    	  case _ => false
    	  }
      } case "_" => _: Any => false
    }
  }

}