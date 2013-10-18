package com.signalcollect.fraudppuccino.componentdetection

import com.signalcollect.fraudppuccino.repeatedanalysis._

// SomeInstance -> ComponentHandler
case class WorkFlowStep(s: String)

// ComponentHandler <-> Master Messages
abstract class HandlerRequest 

case object ComponentSizeQuery extends HandlerRequest
case class ComponentReply(componentId: Any, reply: Option[Any])
case class ComponentAnnouncement(componentId: Any)
case object ComponentElimination extends HandlerRequest
case object ComponentSerialization extends HandlerRequest
case class ComponentSerializationReply(componentJSON: String)

// Master <-> Member Messages
abstract class ComponentMemberMessage //Message sent from the component member to the master
case class ComponentMemberRegistration(neighbors: Iterable[Any]) extends ComponentMemberMessage
case class ComponentMemberQuery(queryFunction: RepeatedAnalysisVertex[_] => ComponentMemberMessage)
case class ComponentMemberResponse(response: Option[Any]) extends ComponentMemberMessage
case class ComponentMemberAlgorithm(algorithmFactory: RepeatedAnalysisVertex[_] => VertexAlgorithm)
case object ComponentMemberElimination
case class ComponentMemberInfo(id: Any, results: collection.Map[String, Any], successors: Iterable[Int]) extends ComponentMemberMessage