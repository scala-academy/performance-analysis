sonatypeProfileName := "com.github.scala-academy"

publishMavenStyle := true

pomIncludeRepository := { _ => false }

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

// To sync with Maven central, you need to supply the following information:
pomExtra in Global := {
  <url>https://github.com/scala-academy/castalia</url>
    <licenses>
      <license>
        <name>MIT</name>
      </license>
    </licenses>
    <scm>
      <connection>scm:git:github.com/scala-academy/castalia.git</connection>
      <developerConnection>scm:git:git@github.com/scala-academy/castalia.git</developerConnection>
      <url>https://github.com/scala-academy/castalia</url>
    </scm>
    <developers>
//     TODO add committers, maybe automagically from git log
    </developers>
}