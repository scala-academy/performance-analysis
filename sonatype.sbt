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
        <id>jwvl</id>
        <name>Jan-Willem van Leussen>
      </developer>
    </developers>
}
