package com.signalcollect.fraudppuccino.componentdetection

import com.signalcollect.fraudppuccino.repeatedanalysis._
import com.signalcollect.GraphEditor
import com.signalcollect.fraudppuccino.resulthandling.ComponentResultHandler

// SomeInstance -> ComponentHandler
case class WorkFlowStep(s: String)
case class RegisterResultHandler(handler: ComponentResultHandler)

// ComponentHandler <-> Master Messages
abstract class HandlerRequest //Handler requests some action at the master
case class ComponentMasterQuery(query: ComponentMaster => Any) extends HandlerRequest
case class ComponentMemberQueryExecution(query: ComponentMemberQuery, allRepliesReceived: (Iterable[ComponentMemberMessage], ComponentMaster, GraphEditor[_, _]) => Unit) extends HandlerRequest
case class ComponentAlgorithmExecution(memberAlgorithm: ComponentMemberAlgorithm, allRepliesReceived: (Iterable[ComponentMemberMessage], ComponentMaster, GraphEditor[_, _]) => Unit) extends HandlerRequest
case object ComponentElimination extends HandlerRequest

case class ComponentReply(componentId: Any, reply: Option[Any])
case class ComponentAnnouncement(componentId: Any)

// Master <-> Member Messages
abstract class ComponentMemberMessage //Message sent from the component member to the master
case class ComponentMemberRegistration(neighbors: Iterable[Any]) extends ComponentMemberMessage
case class ComponentMemberResponse(response: Option[Any]) extends ComponentMemberMessage
case class ComponentMemberInfo(id: Any, results: collection.Map[String, Any], successors: Iterable[Int]) extends ComponentMemberMessage

abstract class MasterRequest //Master requests some action from its members
case class ComponentMemberQuery(queryFunction: ComponentMember => ComponentMemberMessage) extends MasterRequest
case class ComponentMemberAlgorithm(algorithmFactory: RepeatedAnalysisVertex[_] => VertexAlgorithm) extends MasterRequest
case object ComponentMemberElimination extends MasterRequest