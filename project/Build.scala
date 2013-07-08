package bijection

import sbt._
import Keys._
import sbtgitflow.ReleasePlugin._
import com.typesafe.tools.mima.plugin.MimaPlugin.mimaDefaultSettings
import com.typesafe.tools.mima.plugin.MimaKeys.previousArtifact
import com.typesafe.sbt.osgi.SbtOsgi._

object BijectionBuild extends Build {
  val sharedSettings = Project.defaultSettings ++ releaseSettings ++ osgiSettings ++ Seq(
    organization := "com.twitter",

    crossScalaVersions := Seq("2.9.3", "2.10.2"),

    scalaVersion := "2.9.3",

    javacOptions ++= Seq("-source", "1.6", "-target", "1.6"),

    javacOptions in doc := Seq("-source", "1.6"),

    libraryDependencies ++= Seq(
      "org.scalacheck" %% "scalacheck" % "1.10.0" % "test",
      "org.scala-tools.testing" %% "specs" % "1.6.9" % "test"
    ),

    resolvers ++= Seq(
      "snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
      "releases"  at "http://oss.sonatype.org/content/repositories/releases"
    ),

    parallelExecution in Test := true,

    scalacOptions ++= Seq("-unchecked", "-deprecation"),

    OsgiKeys.importPackage <<= scalaVersion { sv => Seq("""scala.*;version="$<range;[==,=+);%s>"""".format(sv)) },

    OsgiKeys.importPackage ++= Seq("com.twitter.bijection.*;version=\"[${Bundle-Version}, ${Bundle-Version}]\"", "*"),

    OsgiKeys.additionalHeaders := Map("-removeheaders" -> "Include-Resource,Private-Package"),

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
  ) ++ mimaDefaultSettings

  // This returns the youngest jar we released that is compatible with the current
  def youngestForwardCompatible(subProj: String) = {
    if(subProj == "netty") None // This is new. Update after next version
    else Some("com.twitter" % ("bijection-" + subProj + "_2.9.3") % "0.5.1")
  }

  def osgiExportAll(packs: String*) = OsgiKeys.exportPackage := packs.map(_ + ".*;version=${Bundle-Version}")

  lazy val bijection = Project(
    id = "bijection",
    base = file("."),
    settings = sharedSettings ++ DocGen.publishSettings
  ).settings(
    test := { },
    publish := { }, // skip publishing for this root project.
    publishLocal := { }
  ).aggregate(bijectionCore,
              bijectionProtobuf,
              bijectionThrift,
              bijectionGuava,
              bijectionScrooge,
              bijectionJson,
              bijectionAlgebird,
              bijectionUtil,
              bijectionClojure,
              bijectionNetty)

  /** No dependencies in bijection other than java + scala */
  lazy val bijectionCore = Project(
    id = "bijection-core",
    base = file("bijection-core"),
    settings = sharedSettings
  ).settings(
    name := "bijection-core",
    previousArtifact := youngestForwardCompatible("core"),
    osgiExportAll("com.twitter.bijection"),
    libraryDependencies ++= Seq(
        "com.novocode" % "junit-interface" % "0.10-M1" % "test",
        "org.scalatest" %% "scalatest" % "1.9.1" % "test"
    )
  )

  lazy val bijectionProtobuf = Project(
    id = "bijection-protobuf",
    base = file("bijection-protobuf"),
    settings = sharedSettings
  ).settings(
    name := "bijection-protobuf",
    previousArtifact := youngestForwardCompatible("protobuf"),
    osgiExportAll("com.twitter.bijection.protobuf"),
    libraryDependencies += "com.google.protobuf" % "protobuf-java" % "2.4.1"
  ).dependsOn(bijectionCore % "test->test;compile->compile")

  val jsonParser = "org.codehaus.jackson" % "jackson-mapper-asl" % "1.8.1"

  lazy val bijectionThrift = Project(
    id = "bijection-thrift",
    base = file("bijection-thrift"),
    settings = sharedSettings
  ).settings(
    name := "bijection-thrift",
    previousArtifact := youngestForwardCompatible("thrift"),
    osgiExportAll("com.twitter.bijection.thrift"),
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
    previousArtifact := youngestForwardCompatible("guava"),
    osgiExportAll("com.twitter.bijection.guava"),
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
    previousArtifact := youngestForwardCompatible("scrooge"),
    osgiExportAll("com.twitter.bijection.scrooge"),
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
    previousArtifact := youngestForwardCompatible("json"),
    osgiExportAll("com.twitter.bijection.json"),
    libraryDependencies += jsonParser
  ).dependsOn(bijectionCore % "test->test;compile->compile")

  lazy val bijectionAlgebird = Project(
    id = "bijection-algebird",
    base = file("bijection-algebird"),
    settings = sharedSettings
  ).settings(
    name := "bijection-algebird",
    previousArtifact := youngestForwardCompatible("algebird"),
    osgiExportAll("com.twitter.bijection.algebird"),
    libraryDependencies += "com.twitter" %% "algebird-core" % "0.1.9" cross CrossVersion.binaryMapped {
      case "2.9.3" => "2.9.2" // TODO: hack because twitter hasn't built things agaisnt 2.9.3
      case version if version startsWith "2.10" => "2.10" // TODO: hack because sbt is broken
      case x       => x
    }
  ).dependsOn(bijectionCore % "test->test;compile->compile")

  lazy val bijectionUtil = Project(
    id = "bijection-util",
    base = file("bijection-util"),
    settings = sharedSettings
  ).settings(
    name := "bijection-util",
    previousArtifact := youngestForwardCompatible("util"),
    osgiExportAll("com.twitter.bijection.twitter_util"),
    libraryDependencies += "com.twitter" %% "util-core" % "6.2.0" cross CrossVersion.binaryMapped {
      case "2.9.3" => "2.9.2" // TODO: hack because twitter hasn't built things against 2.9.3
      case version if version startsWith "2.10" => "2.10" // TODO: hack because sbt is broken
      case x       => x
    }
  ).dependsOn(bijectionCore % "test->test;compile->compile")

  lazy val bijectionClojure = Project(
    id = "bijection-clojure",
    base = file("bijection-clojure"),
    settings = sharedSettings
  ).settings(
    name := "bijection-clojure",
    previousArtifact := youngestForwardCompatible("clojure"),
    osgiExportAll("com.twitter.bijection.clojure"),
    libraryDependencies += "org.clojure" % "clojure" % "1.4.0"
  ).dependsOn(bijectionCore % "test->test;compile->compile")

  lazy val bijectionNetty = Project(
    id = "bijection-netty",
    base = file("bijection-netty"),
    settings = sharedSettings
  ).settings(
    name := "bijection-netty",
    previousArtifact := youngestForwardCompatible("netty"),
    osgiExportAll("com.twitter.bijection.netty"),
    libraryDependencies += "io.netty" % "netty" % "3.5.5.Final"
  ).dependsOn(bijectionCore % "test->test;compile->compile")
}
