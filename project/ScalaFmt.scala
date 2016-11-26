package bijection

import sbt._
import sbt.Keys._

object ScalaFmt extends AutoPlugin with ScalaFmtKeys {

  override def requires = plugins.JvmPlugin
  override def trigger = AllRequirements

  object autoImport extends ScalaFmtKeys

  lazy val scalafmtStub = {
    Project(base = file("project/scalafmt"), id = "scalafmt-stub").settings(
      libraryDependencies ++= {
        if (!scalaVersion.value.startsWith("2.10")) Seq("com.geirsson" %% "scalafmt-cli" % "0.4.10")
        else Seq.empty
      },
      mainClass in Compile := {
        if (!scalaVersion.value.startsWith("2.10")) Some("org.scalafmt.cli.Cli")
        else None
      }
    )
  }
  override def extraProjects = Seq(scalafmtStub)
  override def projectSettings = Seq(
    scalafmtTest := Def.taskDyn {
      if (!scalaVersion.value.startsWith("2.10")) {
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
      } else Def.task { () }
    }.value
  )
}

trait ScalaFmtKeys {
  val scalafmtTest = taskKey[Unit]("Run scalafmt --test for the current project")
}

