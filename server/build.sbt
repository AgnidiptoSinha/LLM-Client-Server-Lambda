ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.12.18"

// Enable ScalaPB
enablePlugins(AkkaGrpcPlugin)

lazy val root = (project in file("."))
  .settings(
    name := "LLM-Microservice"
  )

val AkkaVersion = "2.6.20"
val AkkaHttpVersion = "10.2.10"
val dl4jVersion = "1.0.0-beta7"
val nd4jVersion = "1.0.0-beta7"
val jtokkitVersion = "1.1.0"
val protobufVersion = "3.21.7"

// Add ScalaPB settings
Compile / PB.targets := Seq(
  PB.gens.java -> (Compile / sourceManaged).value,
  scalapb.gen(grpc = true) -> (Compile / sourceManaged).value / "scalapb"
)

libraryDependencies ++= Seq(
  // Akka
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,

  // JSON handling
  "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,

  // Logging
  "ch.qos.logback" % "logback-classic" % "1.2.11",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",

  // Configuration
  "com.typesafe" % "config" % "1.4.2",

  // Testing
  "org.scalatest" %% "scalatest" % "3.2.15" % Test,

  // AWS Dependencies
  "software.amazon.awssdk" % "bedrock" % "2.21.45",
  "software.amazon.awssdk" % "bedrockruntime" % "2.21.45",
  "software.amazon.awssdk" % "lambda" % "2.21.45",
  "software.amazon.awssdk" % "netty-nio-client" % "2.21.45",
  "org.scala-lang.modules" %% "scala-java8-compat" % "0.8.0",

  // ScalaPB and gRPC dependencies
  "com.google.protobuf" % "protobuf-java" % protobufVersion,
  "com.google.protobuf" % "protobuf-java-util" % protobufVersion,
  "io.grpc" % "grpc-netty-shaded" % "1.42.1",
  "io.grpc" % "grpc-protobuf" % "1.42.1",
  "io.grpc" % "grpc-stub" % "1.42.1",

  // ScalaPB specific dependencies
  "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
  "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion
)

// Add sbt-protoc plugin
addSbtPlugin("com.thesamet" % "sbt-protoc" % "1.0.6")

// Assembly settings
assembly / assemblyMergeStrategy := {
  case "reference.conf" => MergeStrategy.concat
  case "application.conf" => MergeStrategy.concat
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

// Simple jar name specification
assembly / assemblyJarName := "app.jar"