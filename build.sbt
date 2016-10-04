lazy val `akka-persistence-test` =
  project.in(file(".")).enablePlugins(AutomateHeaderPlugin, GitVersioning)

libraryDependencies ++= Vector(
  Library.scalaTest % "test"
)

initialCommands := """|import com.tecnoguru.akka.persistence.test._
                      |""".stripMargin
