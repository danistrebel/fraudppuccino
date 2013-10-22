package com.signalcollect.fraudppuccino.componentdetection

import scala.collection.mutable.Map
import com.signalcollect.fraudppuccino.patternanalysis._
import com.signalcollect.GraphEditor
import scala.collection.mutable.ArrayBuffer

/**
 * Utility to facilitate the parsing of filter executions on reported transactions
 */
object ComponentAlgorithmParser {

  /**
   * Registered algorithms that can be used by the parser
   */
  val algorithms = Map[String, HandlerRequest]()

  //Default algorithm implementations

  /**
   * Queries the master for the size of its component
   */
  val sizeQuery = ComponentMasterQuery(master => master.members.size)

  val depthMemberAlgorithm = ComponentMemberAlgorithm(vertex => new PatternDepthAnalyzer(vertex))
  val maxDepthAggregator: (Iterable[ComponentMemberMessage], ComponentMaster, GraphEditor[_, _]) => Unit = {
    (repliesFromMembers, master, graphEditor) =>
      {
        val replies = repliesFromMembers.asInstanceOf[ArrayBuffer[ComponentMemberResponse]]
        val maxDepth = replies.map(_.response.getOrElse(0).asInstanceOf[Int]).max
        graphEditor.sendToActor(master.handler, ComponentReply(master.componentId, Some(maxDepth)))
      }
  }

  /**
   * Queries the component for the max depth i.e. the longest path from any source to a sink transaction
   */
  val depthAlgorithm = ComponentAlgorithmExecution(depthMemberAlgorithm, maxDepthAggregator)

  // Register predefined algorithms
  algorithms += (("size", sizeQuery))
  algorithms += (("depth", depthAlgorithm))
  
  /**
   * Registers an algorithm so that it can be used in a filter step
   */ 
  def registerAlgorithm(key: String, request: HandlerRequest) = algorithms += ((key, request))

  /**
   * Generates a work flow step that consists of the request that is sent to the component master
   * and the condition to determine whether the returned result is accepted or not.
   */
  def parseWorkFlowStep(s: String): (HandlerRequest, Any => Boolean) = {
    val WorkFlowStep = "([\\w]+)\\s*(<|>|<=|>=|=)\\s*([\\w]+)".r
    try {
      val WorkFlowStep(algorithm, operator, value) = s
      val request = algorithms.get(algorithm.toLowerCase)
      if (request.isDefined) {
        val comparisonFunction = parseComparisonFunction(operator, value)
        (request.get, comparisonFunction)
      }
      else {
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