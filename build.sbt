name := "MissaJuna"

version := "1.0-SNAPSHOT"

scalaVersion := "2.10.2"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.2.1",
  jdbc,
  anorm,
  cache,
  "postgresql" % "postgresql" % "8.4-702.jdbc4"
)     

play.Project.playScalaSettings
