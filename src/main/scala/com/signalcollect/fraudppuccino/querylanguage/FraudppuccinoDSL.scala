package com.signalcollect.fraudppuccino.querylanguage

import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor

/**
 * parses a YAML execution specification and starts its execution.
 */
object FRAUDPPUCCINO {

  def execute(s: String) {
    val yaml = new Yaml(new Constructor(classOf[ExecutionModel]))
    val model = yaml.load(s).asInstanceOf[ExecutionModel]
    var execution = model.parseExecution
    execution.execute
  }
}