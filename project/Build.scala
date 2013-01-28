import sbt._
import Keys._

object BijectionBuild extends Build {
  val sharedSettings = Project.defaultSettings ++ Seq(
    organization := "com.twitter",
    version := "0.2.2-SNAPSHOT",
    scalaVersion := "2.9.2",
    libraryDependencies ++= Seq(
      "org.scalacheck" %% "scalacheck" % "1.10.0" % "test" withSources(),
      "org.scala-tools.testing" % "specs_2.9.1" % "1.6.9" % "test" withSources()
    ),

    resolvers ++= Seq(
      "snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
      "releases"  at "http://oss.sonatype.org/content/repositories/releases"
    ),

    parallelExecution in Test := true,

    scalacOptions ++= Seq("-unchecked", "-deprecation"),

    // Publishing options:
    publishMavenStyle := true,

    publishArtifact in Test := false,

    pomIncludeRepository := { x => false },

    publishTo <<= version { (v: String) =>
      val nexus = "https://oss.sonatype.org/"
      if (v.trim.endsWith("SNAPSHOT"))
        Some("sonatype-snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("sonatype-releases"  at nexus + "service/local/staging/deploy/maven2")
    },

    pomExtra := (
      <url>https://github.com/twitter/bijection</url>
      <licenses>
        <license>
          <name>Apache 2</name>
          <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
          <distribution>repo</distribution>
          <comments>A business-friendly OSS license</comments>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:twitter/bijection.git</url>
        <connection>scm:git:git@github.com:twitter/bijection.git</connection>
      </scm>
      <developers>
        <developer>
          <id>oscar</id>
          <name>Oscar Boykin</name>
          <url>http://twitter.com/posco</url>
        </developer>
        <developer>
          <id>oscar</id>
          <name>Marius Eriksen</name>
          <url>http://twitter.com/marius</url>
        </developer>
        <developer>
          <id>sritchie</id>
          <name>Sam Ritchie</name>
          <url>http://twitter.com/sritchie</url>
        </developer>
      </developers>)
  )

  lazy val bijection = Project(
    id = "bijection",
    base = file(".")
    ).settings(
    test := { },
    publish := { }, // skip publishing for this root project.
    publishLocal := { }
  ).aggregate(bijectionCore,
              // TODO: Add back in once we can figure out how to run the tests for these on travis.
              // bijectionProtobuf,
              // bijectionThrift,
              bijectionGuava,
              bijectionScrooge,
              bijectionJson)

  lazy val bijectionCore = Project(
    id = "bijection-core",
    base = file("bijection-core"),
    settings = sharedSettings
  ).settings(
    name := "bijection-core",
    libraryDependencies ++= Seq(
        "commons-codec" % "commons-codec" % "1.7",
        "com.novocode" % "junit-interface" % "0.10-M1" % "test",
        "org.scalatest" %% "scalatest" % "1.6.1" % "test"
    )
  )

  lazy val bijectionProtobuf = Project(
    id = "bijection-protobuf",
    base = file("bijection-protobuf"),
    settings = sharedSettings
  ).settings(
    name := "bijection-protobuf",
    libraryDependencies += "com.google.protobuf" % "protobuf-java" % "2.4.1"
  ).dependsOn(bijectionCore % "test->test;compile->compile")

  val jsonParser = "org.codehaus.jackson" % "jackson-mapper-asl" % "1.8.1"

  lazy val bijectionThrift = Project(
    id = "bijection-thrift",
    base = file("bijection-thrift"),
    settings = sharedSettings
  ).settings(
    name := "bijection-thrift",
    libraryDependencies ++= Seq(
      "org.apache.thrift" % "libthrift" % "0.6.1" exclude("junit", "junit"),
      jsonParser
    )
  ).dependsOn(bijectionCore % "test->test;compile->compile")

  lazy val bijectionGuava = Project(
    id = "bijection-guava",
    base = file("bijection-guava"),
    settings = sharedSettings
  ).settings(
    name := "bijection-guava",
    libraryDependencies ++= Seq(
      // This dependency is required due to a bug with guava 13.0, detailed here:
      // http://code.google.com/p/guava-libraries/issues/detail?id=1095
      "com.google.code.findbugs" % "jsr305" % "1.3.+",
      "com.google.guava" % "guava" % "13.0"
    )
  ).dependsOn(bijectionCore % "test->test;compile->compile")

  lazy val bijectionScrooge = Project(
    id = "bijection-scrooge",
    base = file("bijection-scrooge"),
    settings = sharedSettings
  ).settings(
    name := "bijection-scrooge",
    libraryDependencies ++= Seq(
      "org.apache.thrift" % "libthrift" % "0.6.1" exclude("junit", "junit"),
      "com.twitter" % "scrooge-runtime" % "3.0.4"
    )
  ).dependsOn(bijectionCore % "test->test;compile->compile")

  lazy val bijectionJson = Project(
    id = "bijection-json",
    base = file("bijection-json"),
    settings = sharedSettings
  ).settings(
    name := "bijection-json",
    libraryDependencies += jsonParser
  ).dependsOn(bijectionCore % "test->test;compile->compile")

  lazy val bijectionUtil = Project(
    id = "bijection-util",
    base = file("bijection-util"),
    settings = sharedSettings
  ).settings(
    name := "bijection-util",
    libraryDependencies += "com.twitter" % "util-core" % "5.3.15"
  ).dependsOn(bijectionCore % "test->test;compile->compile")
}
