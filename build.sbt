val commonSettings = Seq(
  organization := "com.praphull",
  scalaVersion := "2.13.4",
  libraryDependencies ++= Seq(guice)
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

      "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test
    )

  )