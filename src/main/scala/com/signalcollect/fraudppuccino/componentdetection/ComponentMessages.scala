package com.signalcollect.fraudppuccino.componentdetection

import com.signalcollect.fraudppuccino.repeatedanalysis._
import com.signalcollect.GraphEditor
import com.signalcollect.fraudppuccino.resulthandling.ComponentResultHandler

// Some Instance -> Component Handler
case class WorkFlowStep(s: String)
case class RegisterResultHandler(handler: ComponentResultHandler)

// Component Handler --> Component Master
case class ComponentWorkflow(workflow: IndexedSeq[(ConditionAlgorithm, Any => Boolean)])
abstract class ConditionAlgorithm //Handler requests some action at the master
case class ComponentMasterQuery(query: ComponentMaster => Any) extends ConditionAlgorithm
case class ComponentMemberQueryExecution(query: ComponentMemberQuery, allRepliesReceived: (Iterable[ComponentMemberMessage], ComponentMaster) => Any) extends ConditionAlgorithm
case class ComponentAlgorithmExecution(memberAlgorithm: ComponentMemberAlgorithm, allRepliesReceived: (Iterable[ComponentMemberMessage], ComponentMaster) => Any) extends ConditionAlgorithm

// Component Master --> Component Handler
case class ComponentAnnouncement(componentId: Any)
case class ComponentResult(serializedComponent: String)
case class ComputationStatus(serializedStatus: String)

// Component Member --> Component Master
abstract class ComponentMemberMessage //Message sent from the component member to the master
case class ComponentMemberRegistration(neighbors: Iterable[Any]) extends ComponentMemberMessage
case class ComponentMemberResponse(response: Option[Any]) extends ComponentMemberMessage
case class ComponentMemberInfo(id: Any, results: collection.Map[String, Any], successors: Iterable[Int]) extends ComponentMemberMessage

// Component Master --> Component Member
abstract class MasterRequest //Master requests some action from its members
case class ComponentMemberQuery(queryFunction: ComponentMember => ComponentMemberMessage) extends MasterRequest
case class ComponentMemberAlgorithm(algorithmFactory: RepeatedAnalysisVertex[_] => VertexAlgorithm) extends MasterRequest
case object ComponentMemberElimination extends MasterRequest