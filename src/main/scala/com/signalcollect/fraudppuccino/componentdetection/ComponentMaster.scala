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
 */
class ComponentMaster(vertex: RepeatedAnalysisVertex[_]) extends ComponentMember(vertex) {

  val componentId = vertex.id

  //Stores the Ids of all the members of the component that it represents
  //will include itself as a member i.e. members.size >= 1
  val members = ArrayBuffer[Any]()

  //All the members that we know of during the registration phase 
  //allows to decide when all members are registered with the master
  val registeredMembersNeighborhood = HashSet[Any]()

  val system = ActorSystemRegistry.retrieve("SignalCollect").get
  val handler = system.actorFor("akka://SignalCollect/user/componentHandler")

  var componentWorkFlow: IndexedSeq[(ConditionAlgorithm, Any => Boolean)] = null
  var workflowIndex = 0

  val repliesFromMembers = ArrayBuffer[ComponentMemberMessage]()
  var allReceived: (Iterable[ComponentMemberMessage], ComponentMaster) => Any = null
  var shouldRequestResults = false //has this master sent algorithms to its members and not yet received their results

  override def deliverSignal(signal: Any, sourceId: Option[Any], graphEditor: GraphEditor[Any, Any]) = {
    signal match {
      case timeOut: Array[Long] => {
        if (shouldRequestResults) {
          val requestState = ComponentMemberQuery(vertex => ComponentMemberResponse(Some(vertex.getState)))
          members.foreach(memberId => {
            graphEditor.sendSignal(requestState, memberId, Some(componentId))
          })
          shouldRequestResults = false
        }
        true
      }
      case ComponentMemberRegistration(neighborhood) => {
        registeredMembersNeighborhood ++= neighborhood
        registeredMembersNeighborhood += sourceId.get
        members += sourceId.get
        if (registeredMembersNeighborhood.size == members.size) {
          registeredMembersNeighborhood.clear
          graphEditor.sendToActor(handler, ComponentAnnouncement(componentId))
        }
        true
      }

      case ComponentWorkflow(wf) => {
        componentWorkFlow = wf
        initializeWorkflow(graphEditor)
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

  def initializeWorkflow(graphEditor: GraphEditor[Any, Any]) {
    if (componentWorkFlow == null) {}
    else {
      executeWorkflowStep(graphEditor)
    }
  }

  def executeWorkflowStep(graphEditor: GraphEditor[Any, Any]) {
    if (workflowIndex >= componentWorkFlow.size) {
      executeAndExpectMemberReplies(memberInfoExtraction, membersSerializer, graphEditor)
    } else {
      val workflowRequest = componentWorkFlow(workflowIndex)._1
      workflowRequest match {
        case ComponentMasterQuery(query) => testWorkflowCondition(query(this), graphEditor)
        case ComponentMemberQueryExecution(memberQuery, resultsProcessing) => {
          executeAndExpectMemberReplies(memberQuery, resultsProcessing, graphEditor)
        }

        case ComponentAlgorithmExecution(algorithm, resultsProcessing) => {
          executeAndExpectMemberReplies(algorithm, resultsProcessing, graphEditor)
          shouldRequestResults = true
        }
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
    } else if (componentWorkFlow(workflowIndex)._2(result)) { //proceed to the next step of the work flow
      workflowIndex += 1
      executeWorkflowStep(graphEditor)
    } else { // drop this component
      dropComponent(graphEditor)
    }
  }

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