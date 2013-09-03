name := "signal-collect-detective"

version := "0.0.1-SNAPSHOT"

organization := "com.signalcollect"

scalaVersion := "2.10.2"

libraryDependencies ++= Seq(
 "org.scala-lang" % "scala-library" % "2.10.2" % "compile",
 "junit" % "junit" % "4.8.2"  % "test",
 "org.specs2" %% "specs2" % "2.1.1" % "test",
 "org.specs2" % "classycle" % "1.4.1" % "test",
 "org.mockito" % "mockito-all" % "1.9.0"  % "test"
)