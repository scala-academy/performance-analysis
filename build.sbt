//import scalariform.formatter.preferences._

name          := """scala-in-practise-base"""
organization  := "com.github.scala-academy"
version       := "0.0.1"
scalaVersion  := "2.11.7"
scalacOptions := Seq("-unchecked", "-feature", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaStreamV      = "1.0"
  val akkaTestkitV     = "2.4.1"
  val scalaTestV       = "3.0.0-M15"
  val finagleV         = "6.31.0"
  Seq(
    "com.typesafe.akka" %% "akka-stream-experimental"             % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-core-experimental"          % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental"    % akkaStreamV,
    "com.typesafe.akka" %% "akka-testkit"                         % akkaTestkitV     % "it,test",
    "com.typesafe.akka" %% "akka-http-testkit-experimental"       % akkaStreamV      % "it,test",
    "org.scalatest"     %% "scalatest"                            % scalaTestV       % "it,test",
    "com.twitter"       %% "finagle-http"                         % finagleV         % "it"
  )
}

lazy val root = project.in(file(".")).configs(IntegrationTest)
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


