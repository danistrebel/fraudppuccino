import sbt._
import Keys._

object EvalBuild extends Build {
   lazy val scCore = ProjectRef(file("../sc/signal-collect"), id = "signal-collect")

   val scEval = Project(id = "signal-collect-evaluation",
                         base = file(".")) dependsOn(scCore)
}
