package com.signalcollect.fraudppuccino.componentdetection

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.SpecificationWithJUnit
import com.signalcollect.fraudppuccino.componentanalysis.ComponentAlgorithmParser

@RunWith(classOf[JUnitRunner])
class WorkFlowParserSpec extends SpecificationWithJUnit {
  "The workflow parser " should {
    "correctly parse operations" in {
      val equals100 = ComponentAlgorithmParser.parseStaticValueComparisonFunction("=", "100")
      equals100(100) === true
      equals100(101) === false
      equals100("some string") === false
      equals100(1.0) === false

      val lessThan100 = ComponentAlgorithmParser.parseStaticValueComparisonFunction("<", "100")
      lessThan100(100l) === false
      lessThan100(99) === true
      lessThan100("some string") must throwA[Exception] //Because only numeric values are allowed here.
      lessThan100(1.0) === true

      val greaterThan100 = ComponentAlgorithmParser.parseStaticValueComparisonFunction(">", "100")
      greaterThan100(100l) === false
      greaterThan100(99) === false
      greaterThan100(101) === true
      
      val greaterEqualThan100 = ComponentAlgorithmParser.parseStaticValueComparisonFunction(">=", "100")
      greaterEqualThan100(100l) === true
      greaterEqualThan100(99) === false
      greaterEqualThan100(101) === true
      
      val approxEquals100 = ComponentAlgorithmParser.parseStaticValueComparisonFunction("~=10%", "100")
      approxEquals100(100) === true
      approxEquals100(110l) === true
      approxEquals100(92.0) === true
      approxEquals100(89.0) === false
    }
  }
}