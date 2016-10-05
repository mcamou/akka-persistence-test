import sbt._

object Version {
  final val Scala     = "2.11.8"
  final val ScalaTest = "3.0.0"
  final val akka      = "2.4.11"
}

object Library {
  val scalaTest = "org.scalatest" %% "scalatest" % Version.ScalaTest

  val akka = Seq(
    "com.typesafe.akka" %% "akka-actor" % Version.akka,
    "com.typesafe.akka" %% "akka-persistence" % Version.akka
  )

  val persistence = Seq(
    "com.github.dnvriend" %% "akka-persistence-jdbc" % "2.6.7",
    "com.typesafe.slick" %% "slick" % "3.1.0",
    "mysql" % "mysql-connector-java" % "6.0.4",
    "org.slf4j" % "slf4j-nop" % "1.6.4"
  )

  val test = Seq(
    scalaTest
  ) map { _ % "test" }

  val all = akka ++ persistence ++ test
}
