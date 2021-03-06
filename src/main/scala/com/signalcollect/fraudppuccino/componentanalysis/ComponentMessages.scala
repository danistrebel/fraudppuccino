package com.signalcollect.fraudppuccino.componentanalysis

import com.signalcollect.fraudppuccino.repeatedanalysis._
import com.signalcollect.GraphEditor
import com.signalcollect.fraudppuccino.resulthandling._

// Some Instance -> Component Handler
case class WorkFlowStep(s: String)
case class RegisterResultHandler(handler:  ComponentResultHandlerFactory)

// Component Handler --> Component Master
case class ComponentWorkflow(workflow: IndexedSeq[ComponentWorkflowStep])
trait ComponentWorkflowStep
case class ConstantWorkflowStep(algorithm: ConditionAlgorithm, acceptance: (Any => Boolean)) extends ComponentWorkflowStep
case class AlgorithmWorkflowStep(algorithm: ConditionAlgorithm, referenceAlgorithm: ConditionAlgorithm, acceptance: ((Any, Any) => Boolean)) extends ComponentWorkflowStep

trait ConditionAlgorithm //Handler requests some action at the master
case class ComponentMasterQuery(query: ComponentMaster => Any) extends ConditionAlgorithm
case class ComponentMemberQueryExecution(query: ComponentMemberQuery, allRepliesReceived: (Iterable[ComponentMemberMessage], ComponentMaster) => Any) extends ConditionAlgorithm
case class ComponentAlgorithmExecution(memberAlgorithm: ComponentMemberAlgorithm, allRepliesReceived: (Iterable[ComponentMemberMessage], ComponentMaster) => Any) extends ConditionAlgorithm

// Component Master --> Component Handler
case class ComponentAnnouncement(componentId: Any)
case class ComponentResult(serializedComponent: String)
case class ComputationStatus(serializedStatus: String)

// Component Member --> Component Master
trait ComponentMemberMessage //Message sent from the component member to the master
case class ComponentMemberRegistration(neighbors: Iterable[Any]) extends ComponentMemberMessage
case class ComponentMemberResponse(response: Option[Any]) extends ComponentMemberMessage
case class ComponentMemberInfo(id: Any, results: collection.Map[String, Any], successors: Iterable[Int]) extends ComponentMemberMessage

// Component Master --> Component Member
trait MasterRequest //Master requests some action from its members
case class ComponentMemberQuery(queryFunction: ComponentMember => ComponentMemberMessage) extends MasterRequest
case class ComponentMemberAlgorithm(algorithmFactory: RepeatedAnalysisVertex[_] => VertexAlgorithm) extends MasterRequest
case object ComponentMemberElimination extends MasterRequest