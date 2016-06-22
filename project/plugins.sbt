resolvers += Classpaths.sbtPluginReleases
resolvers += "Typesafe Repository" at "https://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.3.0")
addSbtPlugin("com.sksamuel.scapegoat" %% "sbt-scapegoat" % "1.0.4")
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.7.0")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.2.0")
addSbtPlugin("io.spray" %% "sbt-revolver" % "0.7.2")
addSbtPlugin("io.gatling" % "gatling-sbt" % "2.2.0")
//addSbtPlugin("com.typesafe.sbt" %% "sbt-native-packager" % "1.0.4")

// Needs to be added to support codacy scala coverage.
// see: https://github.com/codacy/sbt-codacy-coverage#sbt-codacy-coverage
addSbtPlugin("com.codacy" % "sbt-codacy-coverage" % "1.2.1")

// For releasing to Maven Central
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "1.1")
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")