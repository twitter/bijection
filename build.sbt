import com.typesafe.tools.mima.plugin.MimaPlugin.mimaDefaultSettings
import com.typesafe.tools.mima.plugin.MimaKeys.{binaryIssueFilters, previousArtifact}
import com.typesafe.sbt.osgi.SbtOsgi._
import ReleaseTransformations._ // for sbt-release.
import bijection._

def isScala210x(scalaVersion: String) = scalaVersion match {
  case version if version startsWith "2.10" => true
  case _ => false
}

scalaVersion in ThisBuild := "2.11.8"

val sharedSettings = osgiSettings ++ Seq(
    organization := "com.twitter",
    crossScalaVersions := Seq("2.10.6", "2.11.8"),
    scalaVersion := "2.11.8",
    javacOptions ++= Seq("-source", "1.6", "-target", "1.6"),
    javacOptions in doc := Seq("-source", "1.6"),
    libraryDependencies ++= Seq(
      "org.scalacheck" %% "scalacheck" % "1.12.2" % "test",
      "org.scalatest" %% "scalatest" % "2.2.4" % "test"
    ),
    resolvers ++= Seq(
      "snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
      "releases" at "https://oss.sonatype.org/content/repositories/releases"
    ),
    parallelExecution in Test := true,
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-language:implicitConversions",
      "-language:higherKinds",
      "-language:existentials",
      "-Xmax-classfile-name",
      "200"), // People using encrypted file-systems can have problems if the names get too long
    // note changing this parameter will change the binaries
    // obviously. When the name is too long, it is hashed with
    // md5.
    scalacOptions ++= {
      if (scalaVersion.value startsWith "2.10") Seq("-Xdivergence211")
      else Seq.empty
    },
    OsgiKeys.importPackage := {
      Seq("""scala.*;version="$<range;[==,=+);%s>"""".format(scalaVersion.value))
    },
    OsgiKeys.importPackage ++= Seq(
      "com.twitter.bijection.*;version=\"[${Bundle-Version}, ${Bundle-Version}]\"",
      "*"
    ),
    OsgiKeys.additionalHeaders := Map("-removeheaders" -> "Include-Resource,Private-Package"),
    // Publishing options:
    releaseCrossBuild := true,
    releasePublishArtifactsAction := PgpKeys.publishSigned.value,
    releaseVersionBump := sbtrelease.Version.Bump.Minor, // need to tweak based on mima results
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { x =>
      false
    },
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      publishArtifacts,
      setNextVersion,
      commitNextVersion,
      ReleaseStep(action = Command.process("sonatypeReleaseAll", _)),
      pushChanges
    ),
    publishTo := Some {
      if (version.value.trim.endsWith("SNAPSHOT")) Opts.resolver.sonatypeSnapshots
      else Opts.resolver.sonatypeStaging
    },
    pomExtra := (<url>https://github.com/twitter/bijection</url>
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

/**
  * This returns the youngest jar we released that is compatible with
  * the current.
  */
val unreleasedModules = Set[String]()

// This returns the youngest jar we released that is compatible with the current
def youngestForwardCompatible(subProj: String) =
  Some(subProj).filterNot(unreleasedModules.contains(_)).map { s =>
    "com.twitter" %% ("bijection-" + s) % "0.9.1"
  }

/**
  * Empty this each time we publish a new version (and bump the minor number)
  */
val ignoredABIProblems = {
  import com.typesafe.tools.mima.core._
  import com.typesafe.tools.mima.core.ProblemFilters._

  Seq(
    exclude[ReversedMissingMethodProblem]("com.twitter.bijection.GeneratedTupleBufferable.tuple1"),
    exclude[ReversedMissingMethodProblem](
      "com.twitter.bijection.twitter_util.UtilBijections.twitter2JavaFutureBijection"),
    exclude[ReversedMissingMethodProblem](
      "com.twitter.bijection.twitter_util.UtilBijections.twitter2JavaFutureInjection"))
}

def osgiExportAll(packs: String*) =
  OsgiKeys.exportPackage := packs.map(_ + ".*;version=${Bundle-Version}")

lazy val bijection = Project(
  id = "bijection",
  base = file("."),
  settings = sharedSettings
).settings(
    test := {},
    publish := {}, // skip publishing for this root project.
    publishLocal := {}
  ).enablePlugins(DocGen)
  .aggregate(
    bijectionCore,
    bijectionProtobuf,
    bijectionThrift,
    bijectionGuava,
    bijectionScrooge,
    bijectionJson,
    bijectionUtil,
    bijectionFinagleMySql,
    bijectionClojure,
    bijectionNetty,
    bijectionAvro,
    bijectionHbase,
    bijectionJodaTime,
    bijectionJson4s,
    bijectionMacros
  )

def module(name: String) = {
  val id = "bijection-%s".format(name)
  Project(id = id,
          base = file(id),
          settings = sharedSettings ++ Seq(Keys.name := id,
                                           previousArtifact := youngestForwardCompatible(name),
                                           binaryIssueFilters ++= ignoredABIProblems))
}

/** No dependencies in bijection other than java + scala */
lazy val bijectionCore = module("core").settings(
  osgiExportAll("com.twitter.bijection"),
  libraryDependencies ++= Seq(
    "com.novocode" % "junit-interface" % "0.10-M1" % "test",
    "org.scalatest" %% "scalatest" % "1.9.1" % "test"
  ),
  sourceGenerators in Compile += Def.task {
    val main = (sourceManaged in Compile).value
    val out = streams.value
    val pkg = main / "scala" / "com" / "twitter" / "bijection"
    def genSrc(name: String, gen: => String) = {
      val srcFile = pkg / name
      IO.write(srcFile, gen)
      out.log.debug("generated %s" format srcFile)
      srcFile
    }
    Seq(
      genSrc("GeneratedTupleBijections.scala", Generator.generate),
      genSrc("GeneratedTupleBuffer.scala", BufferableGenerator.generate)
    )
  }.taskValue
)

lazy val bijectionProtobuf = module("protobuf")
  .settings(
    osgiExportAll("com.twitter.bijection.protobuf"),
    libraryDependencies ++= Seq(
      "com.google.protobuf" % "protobuf-java" % "2.4.1"
    )
  )
  .dependsOn(bijectionCore % "test->test;compile->compile")

val jsonParser = "org.codehaus.jackson" % "jackson-mapper-asl" % "1.9.2"

lazy val bijectionThrift = module("thrift")
  .settings(
    osgiExportAll("com.twitter.bijection.thrift"),
    libraryDependencies ++= Seq(
      "org.apache.thrift" % "libthrift" % "0.6.1" exclude ("junit", "junit"),
      jsonParser
    )
  )
  .dependsOn(bijectionCore % "test->test;compile->compile", bijectionMacros)

lazy val bijectionGuava = module("guava")
  .settings(
    osgiExportAll("com.twitter.bijection.guava"),
    libraryDependencies ++= Seq(
      // This dependency is required because scalac needs access to all java
      // runtime annotations even though javac does not as detailed here:
      // http://code.google.com/p/guava-libraries/issues/detail?id=1095
      "com.google.code.findbugs" % "jsr305" % "1.3.9",
      "com.google.guava" % "guava" % "14.0"
    )
  )
  .dependsOn(bijectionCore % "test->test;compile->compile")

lazy val bijectionScrooge = module("scrooge")
  .settings(
    osgiExportAll("com.twitter.bijection.scrooge"),
    libraryDependencies ++= Seq(
      "org.apache.thrift" % "libthrift" % "0.6.1" exclude ("junit", "junit"),
      "com.twitter" %% "scrooge-serializer" % "3.17.0",
      "com.twitter" %% "util-core" % "6.24.0",
      "com.twitter" %% "finagle-core" % "6.25.0" % "test"
    )
  )
  .dependsOn(bijectionCore % "test->test;compile->compile", bijectionMacros, bijectionThrift)

lazy val bijectionJson = module("json")
  .settings(
    osgiExportAll("com.twitter.bijection.json"),
    libraryDependencies += jsonParser
  )
  .dependsOn(bijectionCore % "test->test;compile->compile")

lazy val bijectionUtil = module("util")
  .settings(
    osgiExportAll("com.twitter.bijection.twitter_util"),
    libraryDependencies += "com.twitter" %% "util-core" % "6.24.0"
  )
  .dependsOn(bijectionCore % "test->test;compile->compile")

lazy val bijectionFinagleMySql = module("finagle-mysql")
  .settings(
    osgiExportAll("com.twitter.bijection.finagle_mysql"),
    libraryDependencies ++= Seq(
      "com.twitter" %% "finagle-mysql" % "6.25.0",
      "com.twitter" %% "util-core" % "6.24.0"
    )
  )
  .dependsOn(bijectionCore % "test->test;compile->compile")

lazy val bijectionClojure = module("clojure")
  .settings(
    osgiExportAll("com.twitter.bijection.clojure"),
    libraryDependencies += "org.clojure" % "clojure" % "1.5.1"
  )
  .dependsOn(bijectionCore % "test->test;compile->compile")

lazy val bijectionNetty = module("netty")
  .settings(
    osgiExportAll("com.twitter.bijection.netty"),
    libraryDependencies += "io.netty" % "netty" % "3.5.11.Final"
  )
  .dependsOn(bijectionCore % "test->test;compile->compile")

lazy val bijectionAvro = module("avro")
  .settings(
    osgiExportAll("com.twitter.bijection.avro"),
    libraryDependencies ++= Seq(
      "org.apache.avro" % "avro" % "1.7.5"
    )
  )
  .dependsOn(bijectionCore % "test->test;compile->compile")

lazy val bijectionHbase = module("hbase")
  .settings(
    osgiExportAll("com.twitter.bijection.hbase"),
    libraryDependencies ++= Seq(
      "org.apache.hbase" % "hbase" % "0.94.4" % "provided->default" exclude ("org.jruby", "jruby-complete"),
      "org.apache.hadoop" % "hadoop-core" % "1.0.4" % "provided->default"
    )
  )
  .dependsOn(bijectionCore % "test->test;compile->compile")
  .dependsOn(bijectionMacros % "compile->compile")

lazy val bijectionJodaTime = module("jodatime")
  .settings(
    osgiExportAll("com.twitter.bijection.jodatime"),
    libraryDependencies ++= Seq(
      "joda-time" % "joda-time" % "2.3",
      "org.joda" % "joda-convert" % "1.6"
    )
  )
  .dependsOn(bijectionCore % "test->test;compile->compile")

lazy val bijectionJson4s = module("json4s")
  .settings(
    osgiExportAll("com.twitter.bijection.json4s"),
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "org.json4s" %% "json4s-native" % "3.2.10",
      "org.json4s" %% "json4s-ext" % "3.2.10"
    )
  )
  .dependsOn(bijectionCore % "test->test;compile->compile")

lazy val bijectionMacros = module("macros")
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-library" % scalaVersion.value,
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "org.scalatest" %% "scalatest" % "2.2.2" % "test"
    ),
    libraryDependencies ++= {
      if (scalaVersion.value.startsWith("2.10")) Seq("org.scalamacros" %% "quasiquotes" % "2.1.0")
      else Seq.empty
    },
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
  )
  .dependsOn(bijectionCore)
