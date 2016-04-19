//import scalariform.formatter.preferences._
import io.gatling.sbt.GatlingPlugin

name          := """scala-in-practise-performance analysis"""
organization  := "com.github.scala-academy"
version       := "0.0.1"
scalaVersion  := "2.11.7"
scalacOptions := Seq("-unchecked", "-feature", "-deprecation", "-encoding", "utf8")

enablePlugins(GatlingPlugin)

libraryDependencies ++= {
  val akkaV            = "2.4.3"
  val scalaTestV       = "3.0.0-M15"
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
    "io.gatling.highcharts" %  "gatling-charts-highcharts"            % gatlingV         % "it,test,perf",
    "io.gatling"            %  "gatling-test-framework"               % gatlingV         % "it,test,perf"
  )
}

def isPerfTest(name: String): Boolean = name endsWith "Simulation"

val PerfTest = config("perf") extend Test

lazy val baseDir: String = s"${System.getProperty("user.dir")}"
lazy val gatlingScalaSource: String = s"$baseDir/src/perf/scala"

//TODO: Gatling tests are not picked up

lazy val root = project.in(file("."))
  .configs(Gatling)
  .configs(PerfTest)
  .configs(IntegrationTest)
  .settings(inConfig(PerfTest)(Defaults.testSettings): _*)
  .settings(scalaSource in Gatling := new File(gatlingScalaSource))
  .settings(testOptions in Gatling := Seq(Tests.Filter(isPerfTest(_))))

Defaults.itSettings
GatlingPlugin.gatlingSettings
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


