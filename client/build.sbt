ThisBuild / version := "0.1"
ThisBuild / scalaVersion := "2.13.12"

val AkkaVersion = "2.6.20"
val AkkaHttpVersion = "10.2.10"
val Slf4jVersion = "1.7.36"  // Match this with Logback version

lazy val root = (project in file("."))
  .settings(
    name := "LLM-Conversational-Agent",

    Compile / unmanagedResourceDirectories += baseDirectory.value / "src" / "main" / "resources",

    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
      "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
      "io.github.ollama4j" % "ollama4j" % "1.0.79",
      "com.typesafe" % "config" % "1.4.2",

      // Updated logging dependencies with consistent versions
      "ch.qos.logback" % "logback-classic" % "1.2.11",
//      "org.slf4j" % "slf4j-api" % Slf4jVersion,
//      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",

      // Explicitly exclude conflicting SLF4J bindings
      "org.slf4j" % "slf4j-nop" % Slf4jVersion % Test,

      // Testing dependencies
      "org.scalatest" %% "scalatest" % "3.2.15" % Test,
      "com.typesafe.akka" %% "akka-http-testkit" % AkkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test
    ),

    // Exclude conflicting SLF4J implementations
    excludeDependencies ++= Seq(
      ExclusionRule("org.slf4j", "slf4j-simple"),
      ExclusionRule("org.slf4j", "slf4j-nop")
    ),

    // Assembly settings
    assembly / assemblyJarName := "app.jar",
    Compile / mainClass := Some("Main"),
    assembly / mainClass := Some("Main"),

    // Updated assembly merge strategy to handle logging configs
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
      case "reference.conf" => MergeStrategy.concat
      case "application.conf" => MergeStrategy.concat
      case "logback.xml" => MergeStrategy.first
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case x if x.endsWith("module-info.class") => MergeStrategy.discard
      case x if x.endsWith(".conf") => MergeStrategy.concat
      case x if x.contains("slf4j") || x.contains("logback") => MergeStrategy.first
      case _ => MergeStrategy.first
    }
  )