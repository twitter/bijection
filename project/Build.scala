import sbt._
import Keys._

object BijectionBuild extends Build {
  lazy val core = Project(id = "bijection-core",
                          base = file("bijection-core"))

  lazy val thrift = Project(id = "bijection-thrift",
                            base = file("bijection-thrift")) dependsOn(core % "test->test;compile->compile")

  // Shared Settings
  organization in ThisBuild := "com.twitter"

  scalaVersion in ThisBuild := "2.9.2"
}
