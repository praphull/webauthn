val commonSettings = Seq(
  organization := "com.praphull",
  scalaVersion := "2.13.4",
  maintainer := "me@praphull.com",
  libraryDependencies ++= Seq(guice),

  //Do not generate docs while creating dist
  Compile / doc / sources := Nil,
  Compile / packageDoc / publishArtifact := false
)

val jacksonVersion = "2.12.2"
val webAuthn4jVersion = "0.14.1.RELEASE"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(commonSettings)
  .settings(
    name := """WebAuthn""",
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      //WebAuthn
      "com.webauthn4j" % "webauthn4j-core" % webAuthn4jVersion,

      //Jackson
      "com.fasterxml.jackson.core" % "jackson-core" % jacksonVersion,
      "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion,
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion,

      "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test,

      //DB
      "com.typesafe.slick" %% "slick" % "3.3.3",
      //"org.slf4j" % "slf4j-nop" % "1.6.4",
      "com.typesafe.slick" %% "slick-hikaricp" % "3.3.3",
      "org.postgresql" % "postgresql" % "42.2.19"
    )

  )