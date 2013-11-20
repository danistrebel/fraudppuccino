package com.signalcollect.fraudppuccino.componentanalysis

import scala.collection.mutable.Map
import ComponentAlgorithms._

/**
 * Utility to facilitate the parsing of filter executions on reported transactions
 */
object ComponentAlgorithmParser {

  /**
   * Registered algorithms that can be used by the parser
   */
  val algorithms = Map[String, ConditionAlgorithm]()

  // Register predefined algorithms
  algorithms += (("size", SizeQuery))
  algorithms += (("depth", DepthAlgorithm))
  algorithms += (("sinkaccounts", SinkAccountCounter))
  algorithms += (("sourceaccounts", SourceAccountCounter))
  algorithms += (("sinktransactions", SinkTransactionCounter))
  algorithms += (("sourcetransactions", SourceTransactionCounter))
  algorithms += (("countryhops", XCountryHops))
  algorithms += (("sinkvalue", SinkValue))
  algorithms += (("sourcevalue", SourceValue))
  algorithms += (("maxtransactionvalue", MaxValue))
  algorithms += (("cashsources", CashSourceCounter))
  algorithms += (("circlemembers", CircleAlgorithm))
  algorithms += (("fairsplits", FairSplitCounter))
  algorithms += (("samedaysplits", SameDaySplitCounter))

  /**
   * Registers an algorithm so that it can be used in a filter step
   */
  def registerAlgorithm(key: String, request: ConditionAlgorithm) = algorithms += ((key, request))

  /**
   * Generates a work flow step that consists of the request that is sent to the component master
   * and the condition to determine whether the returned result is accepted or not.
   */
  def parseWorkFlowStep(s: String): ComponentWorkflowStep = {
    val WorkFlowStep = "([\\w]+)\\s*(<|>|<=|>=|=)\\s*([\\w]+)".r
    try {
      val WorkFlowStep(algorithm, operator, referenceValue) = s
      val componentAlgorithm = algorithms.get(algorithm.toLowerCase)
      val referenceAlgorithm = algorithms.get(referenceValue.toLowerCase)

      if (componentAlgorithm.isDefined && referenceAlgorithm.isDefined) {
        AlgorithmWorkflowStep(componentAlgorithm.get, referenceAlgorithm.get, parseAlgorithmValueComparisonFunction(operator))
      } else if (componentAlgorithm.isDefined) {
        ConstantWorkflowStep(componentAlgorithm.get, parseStaticValueComparisonFunction(operator, referenceValue))
      } else {
        throw new Exception("no algorithm namded \"" + algorithm + "\" is registered. Check spellling or register this algorithm with the ComponentAlgorithmParser object.")
      }
    } catch {
      case t: Throwable => {
        System.err.println("Error while parsing: " + s)
        t.printStackTrace
        throw t
      }
    }
  }

  /**
   * Generates the evaluation function that determines whether a returned result is accepted.
   */
  def parseStaticValueComparisonFunction(operator: String, value: String): Any => Boolean = {
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

  def parseAlgorithmValueComparisonFunction(operator: String): (Any, Any) => Boolean = {
    operator match {
      case "=" => (resultA, resultB) => resultA.toString == resultB.toString
      case ">" => (resultA, resultB) => {
        resultA match {
          case res: Long => res.asInstanceOf[Long] > resultB.asInstanceOf[Long]
          case res: Int => res.asInstanceOf[Int] > resultB.asInstanceOf[Int]
          case res: Double => res.asInstanceOf[Double] > resultB.asInstanceOf[Double]
          case res: Float => res.asInstanceOf[Float] > resultB.asInstanceOf[Float]
          case _ => false
        }
      }
      case "<" => (resultA, resultB) => {
        resultA match {
          case res: Long => res.asInstanceOf[Long] < resultB.asInstanceOf[Long]
          case res: Int => res.asInstanceOf[Int] < resultB.asInstanceOf[Int]
          case res: Double => res.asInstanceOf[Double] < resultB.asInstanceOf[Double]
          case res: Float => res.asInstanceOf[Float] < resultB.asInstanceOf[Float]
          case _ => false
        }
      }
      case "<=" => (resultA, resultB) => {
        resultA match {
          case res: Long => res.asInstanceOf[Long] <= resultB.asInstanceOf[Long]
          case res: Int => res.asInstanceOf[Int] <= resultB.asInstanceOf[Int]
          case res: Double => res.asInstanceOf[Double] <= resultB.asInstanceOf[Double]
          case res: Float => res.asInstanceOf[Float] <= resultB.asInstanceOf[Float]
          case _ => false
        }
      }
      case ">=" => (resultA, resultB) => {
        resultA match {
          case res: Long => res.asInstanceOf[Long] >= resultB.asInstanceOf[Long]
          case res: Int => res.asInstanceOf[Int] >= resultB.asInstanceOf[Int]
          case res: Double => res.asInstanceOf[Double] >= resultB.asInstanceOf[Double]
          case res: Float => res.asInstanceOf[Float] >= resultB.asInstanceOf[Double]
          case _ => false
        }
      } case "_" => (resultA, resultB) => false
    }
  }

}