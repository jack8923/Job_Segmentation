name := "allaboutscala"

version := "0.1"

scalaVersion := "2.13.4"

useCoursier := false

libraryDependencies ++= Seq(
  "joda-time" % "joda-time" % "2.9.3",
  "org.joda" % "joda-convert" % "1.8",
  "com.typesafe.akka" %% "akka-actor"   % "2.5.26",
  "com.typesafe.akka" %% "akka-http"   % "10.1.11",
  "com.typesafe.akka" %% "akka-http-spray-json"   % "10.1.11",
  "com.typesafe.akka" %% "akka-stream"   % "2.6.12",
  "net.liftweb" %% "lift-json" % "3.4.1",
  "com.typesafe.akka" %% "akka-serialization-jackson" % "2.6.12",
  "org.scalatest" %% "scalatest" % "3.0.8" % Test,
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.10.5",
  "com.amazonaws" % "aws-java-sdk" % "1.0.002",
  "org.mongodb.scala" %% "mongo-scala-driver" % "2.9.0",
)

