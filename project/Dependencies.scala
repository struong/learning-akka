import sbt._

object Dependencies {
  type Version = String

  object Versions {
    val ScalaTest: Version = "3.2.8"

    val Cats: Version = "2.6.0"
    val CatsEffect: Version = "3.1.1"

    val LogbackClassic: Version = "1.2.3"
    val ScalaLogging: Version = "3.9.3"

    val Akka: Version = "2.6.14"
    val AkkaHttp: Version = "10.2.4"
  }

  val scalaTest = Seq(
    "org.scalatest" %% "scalatest" % Versions.ScalaTest
  )

  val logging = Seq(
    "ch.qos.logback" % "logback-classic" % Versions.LogbackClassic,
    "com.typesafe.scala-logging" %% "scala-logging" % Versions.ScalaLogging
  )


  val cats = Seq(
    "org.typelevel" %% "cats-core" % Versions.Cats,
    "org.typelevel" %% "cats-effect" % Versions.CatsEffect
  )

  val akka = Seq(
    // Classic
    "com.typesafe.akka" %% "akka-actor" % Versions.Akka,
    "com.typesafe.akka" %% "akka-testkit" % Versions.Akka % Test,

    // Typed
    "com.typesafe.akka" %% "akka-actor-typed" % Versions.Akka,
    "com.typesafe.akka" %% "akka-actor-testkit-typed" % Versions.Akka % Test,

    // Http
    "com.typesafe.akka" %% "akka-stream" % Versions.Akka,
    "com.typesafe.akka" %% "akka-http" % Versions.AkkaHttp,
    "com.typesafe.akka" %% "akka-http-xml" % Versions.AkkaHttp,

    // Persistance
    "com.typesafe.akka" %% "akka-persistence-cassandra" % "1.0.5",
    "com.typesafe.akka" %% "akka-persistence" % Versions.Akka,
    "com.typesafe.akka" %% "akka-persistence-query" % Versions.Akka,
    "com.typesafe.akka" %% "akka-cluster-tools" % Versions.Akka
  )
}
