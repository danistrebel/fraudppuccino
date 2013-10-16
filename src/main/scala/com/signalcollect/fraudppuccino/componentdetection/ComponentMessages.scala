package com.signalcollect.fraudppuccino.componentdetection

import com.signalcollect.fraudppuccino.repeatedanalysis._

// ComponentHandler <-> Master Messages
case object ComponentSizeQuery
case class ComponentSizeReply(componentId: Any, componentSize: Int)
case class ComponentAnnouncement(componentId: Any)
case object ComponentElimination

// Master <-> Member Messages
case object ComponentMemberRegistration
case class ComponentMemberQuery(key: String)
case class ComponentMemberResponse(response: Option[Any])
case class ComponentMemberAlgorithm(algorithmFactory: RepeatedAnalysisVertex[_] => VertexAlgorithm)
case object ComponentMemberElimination
