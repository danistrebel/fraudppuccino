import AssemblyKeys._

assemblySettings

excludedJars in assembly <<= (fullClasspath in assembly) map { cp => 
  cp filter {_.data.getName == "minlog-1.2.jar"}
}

name := "signal-collect-fraudppucchino"

version := "0.0.1-SNAPSHOT"

scalacOptions ++= Seq("-optimize")

organization := "com.signalcollect"

scalaVersion := "2.10.3"

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

EclipseKeys.withSource := true

libraryDependencies ++= Seq(
 "org.scala-lang" % "scala-library" % "2.10.3" % "compile",
 "junit" % "junit" % "4.8.2"  % "test",
 "org.specs2" %% "specs2" % "2.1.1" % "test",
 "org.specs2" % "classycle" % "1.4.1" % "test",
 "org.mockito" % "mockito-all" % "1.9.0"  % "test",
 "org.mashupbots.socko" %% "socko-webserver" % "0.3.0"
)

jarName in assembly := "fraudppucchino.jar"

//test in assembly := {} skips the tests

mainClass in assembly := Some("com.signalcollect.fraudppucchino.evaluation.btc.BTCInputAddressMerger")