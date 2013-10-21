package com.signalcollect.fraudppuccino.componentdetection

import com.signalcollect.fraudppuccino.repeatedanalysis._
import com.signalcollect.GraphEditor

// SomeInstance -> ComponentHandler
case class WorkFlowStep(s: String)
case class RegisterResultHandler(handler: ComponentResultHandler)

// ComponentHandler <-> Master Messages
abstract class HandlerRequest //Handler requests some action at the master
case class ComponentMasterQuery(query: ComponentMaster => Any, label: Option[String] = None) extends HandlerRequest
case class ComponentMemberQueryExecution(query: ComponentMemberQuery, allRepliesReceived: (Iterable[ComponentMemberMessage], ComponentMaster, GraphEditor[_, _]) => Unit)
case class ComponentAlgorithmExecution(memberAlgorithm: RepeatedAnalysisVertex[_] => VertexAlgorithm) extends HandlerRequest
case object ComponentElimination extends HandlerRequest

case class ComponentReply(componentId: Any, reply: Option[Any])
case class ComponentAnnouncement(componentId: Any)

// Master <-> Member Messages
abstract class ComponentMemberMessage //Message sent from the component member to the master
case class ComponentMemberRegistration(neighbors: Iterable[Any]) extends ComponentMemberMessage
case class ComponentMemberResponse(response: Option[Any]) extends ComponentMemberMessage
case class ComponentMemberInfo(id: Any, results: collection.Map[String, Any], successors: Iterable[Int]) extends ComponentMemberMessage

case class ComponentMemberAlgorithm(algorithmFactory: RepeatedAnalysisVertex[_] => VertexAlgorithm)
case object ComponentMemberElimination
case class ComponentMemberQuery(queryFunction: RepeatedAnalysisVertex[_] => ComponentMemberMessage)