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
    val WorkFlowStep = "([\\w]+)\\s*(<|>|<=|>=|=|~=\\d+%)\\s*([\\w]+)".r
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
   * Regex for approximately equal checks
   */
  val ApproxEqualRegex = "~=(\\d+)%".r

  /**
   * Generates the evaluation function that determines whether a returned result is accepted.
   */
  def parseStaticValueComparisonFunction(operator: String, value: String): Any => Boolean = {
    operator match {
      case "=" => Equal.staticCompare(value)
      case ApproxEqualRegex(percentString) => ApproxEqual(percentString).staticCompare(value)
      case ">" => GreaterThan.staticCompare(value)
      case "<" => SmallerThan.staticCompare(value)
      case "<=" => SmallerThanEqual.staticCompare(value)
      case ">=" => GreaterThanEqual.staticCompare(value)
      case "_" => _ => throw new Exception("unsupported operator: " + operator)
    }
  }

  def parseAlgorithmValueComparisonFunction(operator: String): (Any, Any) => Boolean = {
    operator match {
      case "=" => Equal.evaluate
      case ApproxEqualRegex(percentString) => ApproxEqual(percentString).evaluate
      case ">" => GreaterThan.evaluate
      case "<" => SmallerThan.evaluate
      case "<=" => SmallerThanEqual.evaluate
      case ">=" => GreaterThanEqual.evaluate
      case "_" => throw new Exception("unsupported operator: " + operator)
    }
  }

}
trait ResultEvaluation extends Serializable {
  def staticCompare(staticValue: Any): (Any) => Boolean = evaluate(_, staticValue)
  def evaluate: (Any, Any) => Boolean
}

case class ApproxEqual(percentString: String) extends ResultEvaluation {
  def evaluate: (Any, Any) => Boolean = {
    (v1, v2) =>
      val percent = percentString.toInt.toDouble / 100
      v1 match {
        case res: Long => Math.abs(res - LongResult(v2)).toDouble / LongResult(v2) <= percent
        case res: Int => Math.abs(res - IntResult(v2)).toDouble / IntResult(v2) <= percent
        case res: Double => Math.abs(res - DoubleResult(v2)) / DoubleResult(v2) <= percent
        case res: Float => Math.abs(res - FloatResult(v2)) / FloatResult(v2) <= percent
        case _ => throw new Exception("unsupported result type: " + v1.getClass)
      }

  }
}

object Equal extends ResultEvaluation {
  def evaluate: (Any, Any) => Boolean = {
    (v1, v2) =>
      v1 match {
        case res: Long => res == LongResult(v2)
        case res: Int => res == IntResult(v2)
        case res: Double => res == DoubleResult(v2)
        case res: Float => res == FloatResult(v2)
        case _ => throw new Exception("unsupported result type: " + v1.getClass)
      }
  }
}

object GreaterThan extends ResultEvaluation {
  def evaluate: (Any, Any) => Boolean = {
    (v1, v2) =>
      v1 match {
        case res: Long => res > LongResult(v2)
        case res: Int => res > IntResult(v2)
        case res: Double => res > DoubleResult(v2)
        case res: Float => res > FloatResult(v2)
        case _ => throw new Exception("unsupported result type: " + v1.getClass)
      }
  }
}

object SmallerThan extends ResultEvaluation {
  def evaluate: (Any, Any) => Boolean = {
    (v1, v2) =>
      v1 match {
        case res: Long => res < LongResult(v2)
        case res: Int => res < IntResult(v2)
        case res: Double => res < DoubleResult(v2)
        case res: Float => res < FloatResult(v2)
        case _ => throw new Exception("unsupported result type: " + v1.getClass)
      }
  }
}

object SmallerThanEqual extends ResultEvaluation {
  def evaluate: (Any, Any) => Boolean = {
    (v1, v2) =>
      v1 match {
        case res: Long => res <= LongResult(v2)
        case res: Int => res <= IntResult(v2)
        case res: Double => res <= DoubleResult(v2)
        case res: Float => res <= FloatResult(v2)
        case _ => throw new Exception("unsupported result type: " + v1.getClass)
      }
  }
}

object GreaterThanEqual extends ResultEvaluation {
  def evaluate: (Any, Any) => Boolean = {
    (v1, v2) =>
      v1 match {
        case res: Long => res >= LongResult(v2)
        case res: Int => res >= IntResult(v2)
        case res: Double => res >= DoubleResult(v2)
        case res: Float => res >= FloatResult(v2)
        case _ => throw new Exception("unsupported result type: " + v1.getClass)
      }
  }
}

object IntResult {
  def apply(value: Any): Int = {
    value match {
      case s: String => s.toInt
      case _ => value.asInstanceOf[Int]
    }
  }
}

object LongResult {
  def apply(value: Any): Long = {
    value match {
      case s: String => s.toLong
      case _ => value.asInstanceOf[Long]
    }
  }
}

object DoubleResult {
  def apply(value: Any): Double = {
    value match {
      case s: String => s.toDouble
      case _ => value.asInstanceOf[Double]
    }
  }
}

object FloatResult {
  def apply(value: Any): Float = {
    value match {
      case s: String => s.toFloat
      case _ => value.asInstanceOf[Float]
    }
  }
}