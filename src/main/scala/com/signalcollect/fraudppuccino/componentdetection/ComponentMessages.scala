package com.signalcollect.fraudppuccino.componentdetection

import com.signalcollect.fraudppuccino.repeatedanalysis._

// ComponentHandler <-> Master Messages
case object ComponentSizeQuery
case class ComponentSizeReply(componentId: Any, componentSize: Int)
case class ComponentAnnouncement(componentId: Any)
case object ComponentElimination
case object ComponentSerialization
case class ComponentSerializationReply(componentJSON: String)

// Master <-> Member Messages
abstract class ComponentMemberMessage //Message sent from the component member to the master
case object ComponentMemberRegistration extends ComponentMemberMessage
case class ComponentMemberQuery(queryFunction: RepeatedAnalysisVertex[_] => ComponentMemberMessage)
case class ComponentMemberResponse(response: Option[Any]) extends ComponentMemberMessage
case class ComponentMemberAlgorithm(algorithmFactory: RepeatedAnalysisVertex[_] => VertexAlgorithm)
case object ComponentMemberElimination
case class ComponentMemberInfo(id: Any, results: collection.Map[String, Any], successors: Iterable[Int]) extends ComponentMemberMessage