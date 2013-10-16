package com.signalcollect.fraudppuccino.componentdetection

import com.signalcollect.fraudppuccino.repeatedanalysis._

// ComponentController -> Master Messages
case class ComponentSizeQuery

// Master -> Member Messages
object ComponentMemberRegistration
case class ComponentMemberQuery(key: String)
case class ComponentMemberResponse(response: Option[Any])
case class ComponentMemberAlgorithm(algorithmFactory: RepeatedAnalysisVertex[_] => VertexAlgorithm)