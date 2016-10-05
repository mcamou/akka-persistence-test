lazy val `akka-persistence-test` =
  project.in(file(".")).enablePlugins(AutomateHeaderPlugin, GitVersioning)

libraryDependencies ++= Library.all

cancelable in Global := true

initialCommands := """|import com.tecnoguru.akka.persistence.test._
                      |""".stripMargin
