import Dependencies._

ThisBuild / version := "0.1.0-SNAPSHOT"

scalaVersion := "2.13.8"

libraryDependencies ++= {
  akka ++ logging
}

lazy val root = (project in file("."))
  .settings(
    name := "learning-akka"
  )

