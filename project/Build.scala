import sbt._
import Keys._

object BijectionBuild extends Build {
  lazy val core = Project(id = "bijection-core",
                          base = file("bijection-core"))

  lazy val thrift = Project(id = "bijection-thrift",
                            base = file("bijection-thrift")) dependsOn(core % "test->test;compile->compile")

  lazy val json = Project(id = "bijection-json",
                          base = file("bijection-json")) dependsOn(core % "test->test;compile->compile")
}
