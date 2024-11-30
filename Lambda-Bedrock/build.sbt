ThisBuild / version := "0.1"
ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .enablePlugins(AkkaGrpcPlugin)  // Add this line
  .settings(
    name := "scala-bedrock-lambda",
    Compile / PB.targets := Seq(
      scalapb.gen() -> (Compile / sourceManaged).value / "scalapb"
    ),
    // Add plugin settings
    akkaGrpcGeneratedLanguages := Seq(AkkaGrpc.Scala),
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-lambda-java-core" % "1.2.2",
      "com.amazonaws" % "aws-lambda-java-events" % "3.11.0",
      "software.amazon.awssdk" % "bedrockruntime" % "2.21.45",
      "io.circe" %% "circe-core" % "0.14.5",
      "io.circe" %% "circe-generic" % "0.14.5",
      "io.circe" %% "circe-parser" % "0.14.5",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",
      "ch.qos.logback" % "logback-classic" % "1.2.11",

      "com.google.protobuf" % "protobuf-java" % "3.21.7",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",

// Test dependencies
      "org.scalatest" %% "scalatest" % "3.2.15" % Test,
      "org.scalamock" %% "scalamock" % "5.2.0" % Test,
      "com.typesafe.akka" %% "akka-http-spray-json" % "10.2.10"
    ),

  )

assembly / assemblyMergeStrategy := {
  case "META-INF/MANIFEST.MF" => MergeStrategy.discard
  case "META-INF/LICENSE" => MergeStrategy.discard
  case "META-INF/NOTICE" => MergeStrategy.discard
  case "META-INF/io.netty.versions.properties" => MergeStrategy.discard
  case x if x.endsWith(".txt") => MergeStrategy.discard
  case x if x.endsWith(".class") => MergeStrategy.first
  case _ => MergeStrategy.first
}