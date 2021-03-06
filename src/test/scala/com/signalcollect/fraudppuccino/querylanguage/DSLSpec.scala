package com.signalcollect.fraudppuccino.querylanguage

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.SpecificationWithJUnit

import FRAUDPPUCCINO._

@RunWith(classOf[JUnitRunner])
class DSLSpec extends SpecificationWithJUnit {
  "the DSL parser " should {
    "correctly parse different time units" in {
      val model = new ExecutionModel
      model.parseSec("2w") === 1209600l
      model.parseSec("3 d") === 259200l
      model.parseSec("4h") === 14400l
      model.parseSec("5min") === 300l
      model.parseSec("6s") === 6l
      model.parseSec("7") === 7l
      model.parseSec("1decade") must throwA[Exception] //decades don't fit into memory ;)
    }

    "correctly parse different unix time stamps" in {
      val model = new ExecutionModel
      model.parseUnixDate("2/3/1988") === 570844800
      model.parseUnixDate("2/3/1988 00:00:00") === 570844800
      model.parseUnixDate("12/31/2013 22:30:00") === 1388529000
      model.parseUnixDate("1388529000") === 1388529000
    }
  }
}