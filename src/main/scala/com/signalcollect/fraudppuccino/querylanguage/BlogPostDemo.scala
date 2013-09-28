package com.signalcollect.fraudppuccino.querylanguage

import language.dynamics

object BlogPostDemo extends App with Dynamic {

  case class Parser(l: List[Any]) extends Dynamic {
    def applyDynamic(name: String)(args: Any*) = Parser(annotate(args(0).toString()) :: annotate(name) :: l)
    def selectDynamic(name: String) = Parser(annotate(name) :: l)
  }
  val parse = Parser(Nil)

  def annotate(term: String) = {
    term match {
      case "all" | "no" => "QUANTIFIER"
      case "elephant" | "roads" => "SUBJECT"
      case "lead" | "is" | "to" => "VERB"
      case _ => term
    }
  }

  val roads = "roads"
  val elephant = "elephant"
  val to = "to"
  val blue = "blue"

  val a = parse all roads lead to Rome
  val b = parse no elephant is blue

  println(a.l.reverse)
  println(b.l.reverse)
}