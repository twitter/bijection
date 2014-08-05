package bijection

import sbt._
import Keys._
import com.typesafe.tools.mima.plugin.MimaPlugin.mimaDefaultSettings
import com.typesafe.tools.mima.plugin.MimaKeys.previousArtifact
import com.typesafe.sbt.osgi.SbtOsgi._
import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform._

object BijectionBuild extends Build {
  def withCross(dep: ModuleID) =
    dep cross CrossVersion.binaryMapped {
      case "2.9.3" => "2.9.2" // TODO: hack because twitter hasn't built things against 2.9.3
      case version if version startsWith "2.10" => "2.10" // TODO: hack because sbt is broken
      case x => x
    }

  val sharedSettings = Project.defaultSettings ++ osgiSettings ++ scalariformSettings ++ Seq(
    organization := "com.twitter",

    crossScalaVersions := Seq("2.9.3", "2.10.4"),

    ScalariformKeys.preferences := formattingPreferences,

    scalaVersion := "2.9.3",

    javacOptions ++= Seq("-source", "1.6", "-target", "1.6"),

    javacOptions in doc := Seq("-source", "1.6"),

    libraryDependencies ++= Seq(
      "org.scalacheck" %% "scalacheck" % "1.10.0" % "test",
      "org.scala-tools.testing" %% "specs" % "1.6.9" % "test"
    ),

    resolvers ++= Seq(
      "snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
      "releases"  at "https://oss.sonatype.org/content/repositories/releases"
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

    publishTo <<= version { v =>
      Some(
        if (v.trim.endsWith("SNAPSHOT"))
          Opts.resolver.sonatypeSnapshots
        else
          Opts.resolver.sonatypeStaging
      )
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


   lazy val formattingPreferences = {
     import scalariform.formatter.preferences._
     FormattingPreferences().
       setPreference(AlignParameters, false).
       setPreference(PreserveSpaceBeforeArguments, true)
  }

  /**
   * This returns the youngest jar we released that is compatible with
   * the current.
   */
  val unreleasedModules = Set[String]()

  // This returns the youngest jar we released that is compatible with the current
  def youngestForwardCompatible(subProj: String) =
    Some(subProj)
      .filterNot(unreleasedModules.contains(_))
      .map { s => "com.twitter" % ("bijection-" + s + "_2.9.3") % "0.6.0" }

  def osgiExportAll(packs: String*) =
    OsgiKeys.exportPackage := packs.map(_ + ".*;version=${Bundle-Version}")

  lazy val bijection = Project(
    id = "bijection",
    base = file("."),
    settings = sharedSettings ++ DocGen.publishSettings
  ).settings(
    test := { },
    publish := { }, // skip publishing for this root project.
    publishLocal := { }
  ).aggregate(
    bijectionCore,
    bijectionProtobuf,
    bijectionThrift,
    bijectionGuava,
    bijectionScrooge,
    bijectionJson,
    bijectionUtil,
    bijectionClojure,
    bijectionNetty,
    bijectionAvro,
    bijectionHbase,
    bijectionJodaTime,
    bijectionJson4s
  )

  def module(name: String) = {
    val id = "bijection-%s".format(name)
    Project(id = id, base = file(id), settings = sharedSettings ++ Seq(
      Keys.name := id,
      previousArtifact := youngestForwardCompatible(name))
    )
  }

  /** No dependencies in bijection other than java + scala */
  lazy val bijectionCore = module("core").settings(
    osgiExportAll("com.twitter.bijection"),
    libraryDependencies ++= Seq(
      "com.novocode" % "junit-interface" % "0.10-M1" % "test",
      "org.scalatest" %% "scalatest" % "1.9.1" % "test"
    ),
    sourceGenerators in Compile <+= (sourceManaged in Compile, streams) map {
      (main, out) =>
      val pkg = main / "scala"/ "com" / "twitter" / "bijection"
      def genSrc(name: String, gen: => String) = {
        val srcFile = pkg / name
        IO.write(srcFile, gen)
        out.log.debug("generated %s" format srcFile)
        srcFile
      }
      Seq(genSrc("GeneratedTupleBijections.scala", Generator.generate),
        genSrc("GeneratedTupleBuffer.scala", BufferableGenerator.generate))
    }
  )

  lazy val bijectionProtobuf = module("protobuf").settings(
    osgiExportAll("com.twitter.bijection.protobuf"),
    libraryDependencies += "com.google.protobuf" % "protobuf-java" % "2.4.1"
  ).dependsOn(bijectionCore % "test->test;compile->compile")

  val jsonParser = "org.codehaus.jackson" % "jackson-mapper-asl" % "1.8.1"

  lazy val bijectionThrift = module("thrift").settings(
    osgiExportAll("com.twitter.bijection.thrift"),
    libraryDependencies ++= Seq(
      "org.apache.thrift" % "libthrift" % "0.6.1" exclude("junit", "junit"),
      jsonParser
    )
  ).dependsOn(bijectionCore % "test->test;compile->compile")

  lazy val bijectionGuava = module("guava").settings(
    osgiExportAll("com.twitter.bijection.guava"),
    libraryDependencies ++= Seq(
      // This dependency is required because scalac needs access to all java
      // runtime annotations even though javac does not as detailed here:
      // http://code.google.com/p/guava-libraries/issues/detail?id=1095
      "com.google.code.findbugs" % "jsr305" % "1.3.9",
      "com.google.guava" % "guava" % "14.0"
    )
  ).dependsOn(bijectionCore % "test->test;compile->compile")

  lazy val bijectionScrooge = module("scrooge").settings(
    osgiExportAll("com.twitter.bijection.scrooge"),
    libraryDependencies ++= Seq(
      "org.apache.thrift" % "libthrift" % "0.6.1" exclude("junit", "junit"),
      withCross("com.twitter" %% "scrooge-serializer" % "3.6.0")
    )
  ).dependsOn(bijectionCore % "test->test;compile->compile")

  lazy val bijectionJson = module("json").settings(
    osgiExportAll("com.twitter.bijection.json"),
    libraryDependencies += jsonParser
  ).dependsOn(bijectionCore % "test->test;compile->compile")

  lazy val bijectionUtil = module("util").settings(
    osgiExportAll("com.twitter.bijection.twitter_util"),
    libraryDependencies += withCross("com.twitter" %% "util-core" % "6.3.0")
  ).dependsOn(bijectionCore % "test->test;compile->compile")

  lazy val bijectionClojure = module("clojure").settings(
    osgiExportAll("com.twitter.bijection.clojure"),
    libraryDependencies += "org.clojure" % "clojure" % "1.5.1"
  ).dependsOn(bijectionCore % "test->test;compile->compile")

  lazy val bijectionNetty = module("netty").settings(
    osgiExportAll("com.twitter.bijection.netty"),
    libraryDependencies += "io.netty" % "netty" % "3.5.11.Final"
  ).dependsOn(bijectionCore % "test->test;compile->compile")

  lazy val bijectionAvro = module("avro").settings(
    osgiExportAll("com.twitter.bijection.avro"),
    libraryDependencies ++= Seq(
      "org.apache.avro" % "avro" % "1.7.5"
    )
  ).dependsOn(bijectionCore % "test->test;compile->compile")

  lazy val bijectionHbase = module("hbase").settings(
    osgiExportAll("com.twitter.bijection.hbase"),
    libraryDependencies ++= Seq(
      "org.apache.hbase" % "hbase" % "0.94.4" % "provided->default",
      "org.apache.hadoop" % "hadoop-core" % "1.0.4" % "provided->default"
    )
  ).dependsOn(bijectionCore % "test->test;compile->compile")

  lazy val bijectionJodaTime = module("jodatime").settings(
    osgiExportAll("com.twitter.bijection.jodatime"),
    libraryDependencies ++= Seq(
      "joda-time" % "joda-time" % "2.2",
      "org.joda" % "joda-convert" % "1.3.1"
    )
  ).dependsOn(bijectionCore % "test->test;compile->compile")

  lazy val bijectionJson4s = module("json4s").settings(
    osgiExportAll("com.twitter.bijection.json4s"),
    libraryDependencies ++= Seq(
      "org.json4s" %% "json4s-native" % "3.2.6",
      "org.json4s" %% "json4s-ext" % "3.2.6"
    )
  ).dependsOn(bijectionCore % "test->test;compile->compile")


}
