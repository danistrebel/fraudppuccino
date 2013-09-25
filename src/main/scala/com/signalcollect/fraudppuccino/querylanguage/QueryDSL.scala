package com.signalcollect.fraudppuccino.querylanguage

import scala.language.dynamics

class QueryDSL extends Dynamic {
  def selectDynamic(name: String) = {
    println("called " + name)
    name
  }
}

object Query extends App {
  val query = new QueryDSL
  query foo
}

object FRAUDPPUCCINO {

  object LOAD {
    def apply(path: String) = RangeParser(path)
  }

  case class RangeParser(path: String = "", start: Int = 0, end: Int = 0) {
    def FROM(i: Int) = this.copy(start = i)
    def TO(i: Int) = this.copy(end = i)
  }

  object RUN {

  }

  object LABEL {

  }
}