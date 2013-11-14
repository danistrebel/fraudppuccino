package com.signalcollect.fraudppuccino.querylanguage

import com.signalcollect.fraudppuccino.evaluation.btc._
import com.signalcollect.fraudppuccino.repeatedanalysis._
import com.signalcollect.fraudppuccino.structuredetection._
import com.signalcollect.fraudppuccino.patternanalysis._
import scala.collection.mutable.HashMap
import language.dynamics
import scala.collection.Iterator
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor

/**
 * DSL to control a fraudppuccino analysis session
 */
object FRAUDPPUCCINO {

  def execute(s: String) {
    val yaml = new Yaml(new Constructor(classOf[ExecutionModel]))
    val model = yaml.load(s).asInstanceOf[ExecutionModel]
    var execution = model.parseExecution
    execution.execute
  }
}