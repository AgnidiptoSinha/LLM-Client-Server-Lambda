ThisBuild / version := "0.1"
ThisBuild / scalaVersion := "2.13.12"

val AkkaVersion = "2.6.20"
val AkkaHttpVersion = "10.2.10"

// Explicitly define logging versions
val Slf4jVersion = "1.7.36"  // This version is compatible with Akka
val LogbackVersion = "1.2.12" // This version works with slf4j 1.7.36

lazy val root = (project in file("."))
  .settings(
    name := "LLM-Conversational-Agent",

    // Force specific versions of logging dependencies
    dependencyOverrides ++= Seq(
      "org.slf4j" % "slf4j-api" % Slf4jVersion,
      "ch.qos.logback" % "logback-classic" % LogbackVersion,
      "ch.qos.logback" % "logback-core" % LogbackVersion
    ),

    libraryDependencies ++= Seq(
      // Akka dependencies
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
      "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,

      // Other dependencies
      "io.github.ollama4j" % "ollama4j" % "1.0.79",
      "com.typesafe" % "config" % "1.4.2",

      // Logging dependencies - explicitly excluding transitive dependencies
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5" excludeAll(
        ExclusionRule(organization = "org.slf4j")
        ),
      "ch.qos.logback" % "logback-classic" % LogbackVersion excludeAll(
        ExclusionRule(organization = "org.slf4j")
        ),
      "org.slf4j" % "slf4j-api" % Slf4jVersion,

      // Testing dependencies
      "org.scalatest" %% "scalatest" % "3.2.15" % Test,
      "com.typesafe.akka" %% "akka-http-testkit" % AkkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test
    ),

    // Assembly settings
    assembly / assemblyJarName := "app.jar",
    Compile / mainClass := Some("Main"),
    assembly / mainClass := Some("Main"),

    // Assembly merge strategy
    assembly / assemblyMergeStrategy := {
      case x if x.contains("module-info.class") => MergeStrategy.discard
      case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
      case "reference.conf" => MergeStrategy.concat
      case "application.conf" => MergeStrategy.concat
      case PathList("META-INF", xs @ _*) =>
        xs.map(_.toLowerCase) match {
          case ("manifest.mf" :: Nil) | ("index.list" :: Nil) | ("dependencies" :: Nil) =>
            MergeStrategy.discard
          case _ => MergeStrategy.first
        }
      case x if x.endsWith(".txt") => MergeStrategy.first
      case x if x.endsWith(".xml") => MergeStrategy.first
      case x if x.endsWith(".properties") => MergeStrategy.first
      case x if x.endsWith(".handlers") => MergeStrategy.first
      case x if x.endsWith(".dtd") => MergeStrategy.first
      case x if x.endsWith(".types") => MergeStrategy.first
      case _ => MergeStrategy.first
    }
  )