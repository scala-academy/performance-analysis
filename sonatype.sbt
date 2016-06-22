sonatypeProfileName := "com.github.scala-academy"

// To sync with Maven central, you need to supply the following information:
pomExtra in Global := {
  <url>https://github.com/scala-academy/performance-analysis</url>
    <licenses>
      <license>
        <name>MIT</name>
      </license>
    </licenses>
    <scm>
      <connection>scm:git@github.com:scala-academy/performance-analysis.git</connection>
      <developerConnection>scm:git@github.com:scala-academy/performance-analysis.git</developerConnection>
      <url>https://github.com/scala-academy/performance-analysis</url>
    </scm>
    <developers>
      <developer>
        <id>TimSoethout</id>
        <name>Tim Soethout</name>
        <url>http://blog.timmybankers.nl</url>
      </developer>
      <developer>
        <id>mihaelaoprea</id>
        <name>Mihaela Oprea</name>
      </developer>
      <developer>
        <id>jordi133</id>
        <name>Jordi de Vos</name>
      </developer>
      <developer>
        <id>effibennekers</id>
        <name>Effi Bennekers</name>
      </developer>
      <developer>
        <id>geerdink</id>
        <name>Bas Geerdink</name>
      </developer>
      <developer>
        <id>Seetaramayya</id>
        <name>Seeta Ramayya Vadali</name>
      </developer>
      <developer>
        <id>ruud</id>
        <name>Ruud Prein</name>
      </developer>
    </developers>
}

credentials ++= (for {
  username <- Option(System.getenv().get("SONATYPE_USERNAME"))
  password <- Option(System.getenv().get("SONATYPE_PASSWORD"))
} yield Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password)).toSeq

