
name := "refined-newtype-examples"
organization := "com.softwaremill"

scalaVersion := "2.13.1"

scalacOptions ++= Seq(
    "-Ymacro-annotations",
    "-Xmaxerrs", "200"
)

val circeVersion = "0.12.3"

libraryDependencies ++= Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser",
    "io.circe" %% "circe-refined"
).map(_ % circeVersion)

libraryDependencies ++= Seq(
    compilerPlugin(
        "org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full
    ),
    "eu.timepit" %% "refined" % "0.9.13",
//    "eu.timepit" %% "refined-cats" % "0.9.13",
    "com.softwaremill.common" %% "tagging" % "2.2.1",
    "io.estatico" %% "newtype" % "0.4.3"
)
