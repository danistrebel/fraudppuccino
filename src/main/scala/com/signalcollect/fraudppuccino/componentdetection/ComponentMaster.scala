package com.signalcollect.fraudppuccino.componentdetection

import com.signalcollect.fraudppuccino.repeatedanalysis.RepeatedAnalysisVertex
import scala.collection.mutable.ArrayBuffer
import com.signalcollect._
import com.signalcollect.configuration.ActorSystemRegistry
import akka.actor.ActorRef
import com.signalcollect.fraudppuccino.structuredetection.DownstreamTransactionPatternEdge
import ch.epfl.lamp.compiler.msil.MemberInfo
import scala.collection.mutable.HashSet

/**
 *  Serves as the main point of access to a connected component.
 *  Component masters are a special version of a component member
 *  therefore play both roles in a connected component.
 */
class ComponentMaster(vertex: RepeatedAnalysisVertex[_]) extends ComponentMember(vertex) {

  val componentId = vertex.id

  /**
   * Stores the Ids of all the members of the component that it represents
   * will include itself as a member i.e. members.size >= 1
   */
  val members = ArrayBuffer[Any]()

  /*
   * All the members that we know of during the registration phase 
   * allows to decide when all members are registered with the master
   */
  val registeredMembersNeighborhood = HashSet[Any]()

  val system = ActorSystemRegistry.retrieve("SignalCollect").get

  /*
   * Component handler that holds the processing work flow
   * that this master and its members have to pass.
   * Additionally the handler is responsible for reporting the 
   * sucessfully matched component to the user.
   */
  val handler = system.actorFor("akka://SignalCollect/user/componentHandler")

  /*
   * The component work flow contains a series of work flow steps that each 
   * have to be passed in order for the component to be regarded as relevant.
   */
  var componentWorkFlow: IndexedSeq[ComponentWorkflowStep] = null

  /**
   * Indicates the current step of the work flow. If the work flow index is
   * out of range of the current work flow the work flow was successfully processed.
   */
  var workflowIndex = 0

  /**
   * Collects replies of asynchronously received results from the users.
   */
  val repliesFromMembers = ArrayBuffer[ComponentMemberMessage]()

  /**
   * Aggregates the results that were sent by the component members once all
   * results were received.
   */
  var allReceived: (Iterable[ComponentMemberMessage], ComponentMaster) => Any = null

  /**
   * If > 0 this means, that this master sent algorithms to its members and
   * has not yet requested their results. If counter is equal to 0 the master
   * will query its members for their results.
   */
  var stepsUntilResultRequest = -1

  /**
   * Used to store the first aggregated result if the work flow step requires
   * the execution of two algorithms that are then compared with each other.
   */
  var comparisonResult: Any = null

  /**
   * Handles signals that are intended for the component master role of this
   * vertex. All other signals are handled by the component member implementation.
   */
  override def deliverSignal(signal: Any, sourceId: Option[Any], graphEditor: GraphEditor[Any, Any]) = {
    signal match {

      case timeOut: Array[Long] => {
        // Steps counter indicates that the master should request the result of the computation
        if (stepsUntilResultRequest == 0) {
          val requestState = ComponentMemberQuery(vertex => ComponentMemberResponse(Some(vertex.getState)))
          members.foreach(memberId => {
            graphEditor.sendSignal(requestState, memberId, Some(componentId))
          })
          stepsUntilResultRequest = -1
        } //Wait for steps counter to time out
        else if (stepsUntilResultRequest > 0) {
          stepsUntilResultRequest -= 1
        }
        true
      }

      case ComponentMemberRegistration(neighborhood) => {
        //To make sure the component is only reported at the handler
        //once all members are registered each member registers itself
        //along with the neighborhood that it knows of.
        registeredMembersNeighborhood ++= neighborhood
        registeredMembersNeighborhood += sourceId.get
        members += sourceId.get

        if (registeredMembersNeighborhood.size == members.size) {
          registeredMembersNeighborhood.clear
          //Report the component and await processing work flow
          graphEditor.sendToActor(handler, ComponentAnnouncement(componentId))
        }
        true
      }

      case ComponentWorkflow(wf) => {
        componentWorkFlow = wf
        executeWorkflowStep(graphEditor)
        true
      }

      case memberMessage: ComponentMemberMessage => {
        repliesFromMembers += memberMessage
        if (repliesFromMembers.size == members.size) {
          val result = allReceived(repliesFromMembers, this)
          testWorkflowCondition(result, graphEditor)
        }
        true
      }

      case _ => super.deliverSignal(signal, sourceId, graphEditor)
    }
  }

  /**
   * Starts a new work flow step or prepares the component for reporting at the handler.
   */
  def executeWorkflowStep(graphEditor: GraphEditor[Any, Any]) {
    //Prepare the component for reporting if all steps of the work flow are passed.
    if (workflowIndex >= componentWorkFlow.size) {
      executeAndExpectMemberReplies(memberInfoExtraction, membersSerializer, graphEditor)
    }
    else {
      val currentWorkflowStep = componentWorkFlow(workflowIndex)
      currentWorkflowStep match {
        case ConstantWorkflowStep(algorithm, _) => executeConditionAlgorithm(algorithm, graphEditor)
        case AlgorithmWorkflowStep(algorithm, _, _) => executeConditionAlgorithm(algorithm, graphEditor)
      }
    }
  }

  /**
   * Request some information about the component based on the condition algorithm.
   */
  def executeConditionAlgorithm(algorithm: ConditionAlgorithm, graphEditor: GraphEditor[Any, Any]) {
    algorithm match {
      case ComponentMasterQuery(query) => testWorkflowCondition(query(this), graphEditor)
      case ComponentMemberQueryExecution(memberQuery, resultsProcessing) => {
        executeAndExpectMemberReplies(memberQuery, resultsProcessing, graphEditor)
      }
      case ComponentAlgorithmExecution(algorithm, resultsProcessing) => {
        executeAndExpectMemberReplies(algorithm, resultsProcessing, graphEditor)
        // Result will be tested in the next step because the termination 
        // of the member algorithm needs to be awaited first.
        stepsUntilResultRequest = 1 
      }
    }
  }

  /**
   * Tests a result against the condition of the work flow
   * If the result is accepted the work flow continues
   * Else the component will be removed from the graph
   */
  def testWorkflowCondition(result: Any, graphEditor: GraphEditor[Any, Any]) {
    if (workflowIndex >= componentWorkFlow.size) { //In case the entire work flow is passed the serialized component is returned
      graphEditor.sendToActor(handler, ComponentResult(result.asInstanceOf[String]))
      dropComponent(graphEditor)
    } else {
      val currentWorkflowStep = componentWorkFlow(workflowIndex)
      currentWorkflowStep match {
        //Result is compared to a constant value
        case ConstantWorkflowStep(_, acceptance) => {
          if (acceptance(result)) {
            workflowIndex += 1
            executeWorkflowStep(graphEditor)
          } else {
            dropComponent(graphEditor)
          }
        }
        //Result is compared to the result of another algorithm
        case AlgorithmWorkflowStep(_, comparisonAlgorithm, acceptance) => {
          //Result is the result of the first algorithm
          if (comparisonResult == null) {
            comparisonResult = result
            executeConditionAlgorithm(comparisonAlgorithm, graphEditor)
          //Result is the result of the second algorithm  
          } else if (acceptance(comparisonResult, result)) {
            comparisonResult = null
            workflowIndex += 1
            executeWorkflowStep(graphEditor)
          } else {
            dropComponent(graphEditor)
          }
        }
      }
    }
  }

  /**
   * Instructs the component members to remove themselves from the graph.
   * The component master will be removed via its underlying component member.
   */
  def dropComponent(graphEditor: GraphEditor[Any, Any]) = {
    members.foreach(memberId => {
      graphEditor.sendSignal(ComponentMemberElimination, memberId, Some(componentId))
    })
  }

  /**
   * Sends a request to all members of the component and sets the aggregation operation to process their replies.
   */
  def executeAndExpectMemberReplies(request: MasterRequest,
    allRepliesReceived: (Iterable[ComponentMemberMessage], ComponentMaster) => Any,
    graphEditor: GraphEditor[Any, Any]) {
    repliesFromMembers.clear
    allReceived = allRepliesReceived

    members.foreach(memberId => {
      graphEditor.sendSignal(request, memberId, Some(componentId))
    })
  }

  /**
   * Requests a serialized version of a component member
   */
  val memberInfoExtraction: ComponentMemberQuery = ComponentMemberQuery(vertex => ComponentMemberInfo(vertex.id, vertex.results, vertex.outgoingEdges.filter(_._2 == DownstreamTransactionPatternEdge).map(_._1.asInstanceOf[Int])))

  /**
   * Combines the serialized information about the component member
   * to the serialized component description.
   */
  val membersSerializer: (Iterable[ComponentMemberMessage], ComponentMaster) => Any = {
    (repliesFromMembers, master) =>
      {
        val memberInfos = repliesFromMembers.asInstanceOf[ArrayBuffer[ComponentMemberInfo]]
        val component = "{" +
          "\"id\" : " + componentId.toString + "," +
          "\"start\":" + memberInfos.map(_.results("time").asInstanceOf[Long]).min * 1000l + "," +
          "\"end\":" + memberInfos.map(_.results("time").asInstanceOf[Long]).max * 1000l + "," +
          "\"flow\":" + memberInfos.map(_.results("value").asInstanceOf[Long]).max + "," +
          "\"members\":[" + memberInfos.map(member => {
            "{\"id\":" + member.id + "," + member.results.map(result => "\"" + result._1 + "\":" + result._2.toString).mkString(",") +
              ",\"successor\":[" + member.successors.mkString(",") + "]}"
          }).toList.mkString(",") +
          "]}"
        component
      }
  }
}