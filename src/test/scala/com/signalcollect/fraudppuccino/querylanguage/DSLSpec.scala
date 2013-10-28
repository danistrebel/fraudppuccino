package com.signalcollect.fraudppuccino.querylanguage

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.SpecificationWithJUnit

import FRAUDPPUCCINO._

@RunWith(classOf[JUnitRunner])
class DSLSpec extends SpecificationWithJUnit {
 "the DSL parser " should {
   "correctly parse different time units" in {
     parseSec("2w") === 1209600l
     parseSec("3 d") === 259200l
     parseSec("4h") === 14400l
     parseSec("5min") === 300l
     parseSec("6s") === 6l
     parseSec("7") === 7l
     parseSec("1decade") must throwA[Exception] //decades don't fit into memory ;)
   }
 }
}