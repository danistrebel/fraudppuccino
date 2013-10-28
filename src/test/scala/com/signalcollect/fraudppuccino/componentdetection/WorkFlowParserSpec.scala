package com.signalcollect.fraudppuccino.componentdetection

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.SpecificationWithJUnit

@RunWith(classOf[JUnitRunner])
class WorkFlowParserSpec extends SpecificationWithJUnit {
  "The workflow parser " should {
    "correctly parse operations" in {
      val equals100 = ComponentAlgorithmParser.parseComparisonFunction("=", "100")
      equals100(100) === true
      equals100(101) === false
      equals100("some string") === false
      equals100(1.0) === false

      val lessThan100 = ComponentAlgorithmParser.parseComparisonFunction("<", "100")
      lessThan100(100l) === false
      lessThan100(99) === true
      lessThan100("some string") === false
      lessThan100(1.0) === true

      val greaterThan100 = ComponentAlgorithmParser.parseComparisonFunction(">", "100")
      greaterThan100(100l) === false
      greaterThan100(99) === false
      greaterThan100(101) === true
    }
  }
}