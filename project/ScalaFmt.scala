package bijection

import sbt._
import sbt.Keys._

object ScalaFmt extends AutoPlugin with ScalaFmtKeys {

  override def requires = plugins.JvmPlugin
  override def trigger = AllRequirements

  object autoImport extends ScalaFmtKeys

  lazy val scalafmtStub = {
    Project(base = file("project/scalafmt"), id = "scalafmt-stub").settings(
      libraryDependencies += "com.geirsson" %% "scalafmt-cli" % "0.4.10",
      mainClass in Compile := Some("org.scalafmt.cli.Cli")
    )
  }
  override def extraProjects = Seq(scalafmtStub)
  override def projectSettings = Seq(
    scalafmtTest := Def.taskDyn {
      val scalaSourceFiles = {
        (unmanagedSources in Compile).value ++ (unmanagedSources in Test).value
      }.filter(_.ext == "scala").flatMap { f =>
        val maybeFile = f relativeTo (baseDirectory in ThisBuild).value
        maybeFile.toSeq
      }.mkString(",")
      val args = Seq(
        "",
        "--test",
        "--config=.scalafmt.conf",
        "--files="++scalaSourceFiles
      ).mkString(" ")
      if (scalaSourceFiles.nonEmpty) {
        (run in (scalafmtStub, Compile)).toTask(args)
      } else Def.task { () }
    }.value
  )
}

trait ScalaFmtKeys {
  val scalafmtTest = taskKey[Unit]("Run scalafmt --test for the current project")
}

