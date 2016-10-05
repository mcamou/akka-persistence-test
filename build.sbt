lazy val `akka-persistence-test` =
  project.in(file(".")).enablePlugins(AutomateHeaderPlugin, GitVersioning)

libraryDependencies ++= Library.all

initialCommands := """|import com.tecnoguru.akka.persistence.test._
                      |""".stripMargin
