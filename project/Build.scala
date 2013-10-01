import sbt._
import Keys._

object FraudppucchinoBuild extends Build {
   lazy val scCore = ProjectRef(file("../sc/signal-collect"), id = "signal-collect")

   val scFraudppucchino = Project(id = "signal-collect-fraudppucchino",
                         base = file(".")) dependsOn(scCore)
                         
  lazy val root = Project("root", file(".")) dependsOn(socko)
  lazy val soundPlayerProject = RootProject(uri("git://github.com/alvinj/SoundFilePlayer.git"))
}