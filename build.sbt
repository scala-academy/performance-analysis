//import scalariform.formatter.preferences._
import io.gatling.sbt.GatlingPlugin

name          := "periphas"
organization  := "com.github.scala-academy"
version       := "0.0.1"
scalaVersion  := "2.11.7"
scalacOptions := Seq("-unchecked", "-feature", "-deprecation", "-encoding", "utf8")

enablePlugins(GatlingPlugin)

libraryDependencies ++= {
  val akkaV            = "2.4.3"
  val scalaTestV       = "3.0.0-RC1"
  val finagleV         = "6.31.0"
  val gatlingV         = "2.2.0"

  Seq(
    "com.typesafe.akka"     %% "akka-stream"                          % akkaV,
    "com.typesafe.akka"     %% "akka-http-core"                       % akkaV,
    "com.typesafe.akka"     %% "akka-http-experimental"               % akkaV,
    "com.typesafe.akka"     %% "akka-http-spray-json-experimental"    % akkaV,
    "com.typesafe.akka"     %% "akka-actor"                           % akkaV,
    "com.typesafe.akka"     %% "akka-testkit"                         % akkaV            % "it,test",
    "com.typesafe.akka"     %% "akka-http-testkit"                    % akkaV            % "it,test",
    "org.scalatest"         %% "scalatest"                            % scalaTestV       % "it,test",
    "com.twitter"           %% "finagle-http"                         % finagleV         % "it",
    "io.gatling.highcharts" %  "gatling-charts-highcharts"            % gatlingV         % "it,test,gatling",
    "io.gatling"            %  "gatling-test-framework"               % gatlingV         % "it,test,gatling"
  )
}

def isPerfTest(name: String): Boolean = name endsWith "Simulation"

lazy val root = project.in(file("."))
  .configs(Gatling)
  .configs(IntegrationTest)
  .settings(GatlingPlugin.gatlingSettings: _*)
  .settings(scalaSource in Gatling := new File(sourceDirectory.value, "perf/scala"))
  .settings(resourceDirectory in Gatling := new File(sourceDirectory.value, "perf/resources"))
  .settings(testOptions in Gatling := Seq(Tests.Filter(isPerfTest(_))))
  .settings(fullClasspath in Gatling += new File(crossTarget.value, "gatling-classes"))

Defaults.itSettings
//scalariformSettings
Revolver.settings
//enablePlugins(JavaAppPackaging)

//ScalariformKeys.preferences := ScalariformKeys.preferences.value
//  .setPreference(AlignSingleLineCaseStatements, true)
//  .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
//  .setPreference(DoubleIndentClassDeclaration, true)

//publishMavenStyle := true
//publishArtifact in Test := false
//pomIncludeRepository := { _ => false }

//publishTo := {
//  val nexus = "https://oss.sonatype.org/"
//  if (isSnapshot.value)
//    Some("snapshots" at nexus + "content/repositories/snapshots")
//  else
//    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
//}

fork in run := true


