import com.typesafe.tools.mima.plugin.MimaPlugin.mimaDefaultSettings
import com.typesafe.tools.mima.plugin.MimaKeys.{mimaBinaryIssueFilters, mimaPreviousArtifacts}
import com.typesafe.sbt.osgi.SbtOsgi
import com.typesafe.sbt.osgi.SbtOsgi.autoImport._
import ReleaseTransformations._ // for sbt-release.
import bijection._

val finagleVersion = "6.45.0"

val scalatestVersion = "3.0.8"
val scalacheckVersion = "1.13.5"

val utilVersion = "6.45.0"

val scroogeSerializerVersion = "4.20.0"

def util(mod: String) =
  "com.twitter" %% (s"util-$mod") % utilVersion % "provided"

def finagle(mod: String) =
  "com.twitter" %% (s"finagle-$mod") % finagleVersion % "provided"

def scroogeSerializer = {
  "com.twitter" %% "scrooge-serializer" % scroogeSerializerVersion % "provided"
}

val buildLevelSettings = Seq(
  organization := "com.twitter",
  crossScalaVersions := Seq("2.11.12", scalaVersion.value),
  javacOptions ++= Seq("-source", "1.6", "-target", "1.6"),
  javacOptions in doc := Seq("-source", "1.6", "-Xlint:deprecation", "-Xlint:unchecked"),
  scalaVersion := "2.12.10",
  scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-language:implicitConversions",
    "-language:higherKinds",
    "-language:existentials",
    "-Xmax-classfile-name",
    "200"
    // People using encrypted file-systems can have problems if the names get too long
    // note changing this parameter will change the binaries
    // obviously. When the name is too long, it is hashed with
    // md5.
  ),
  resolvers ++= Seq(
    "snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
    "releases" at "https://oss.sonatype.org/content/repositories/releases"
  ),
  libraryDependencies ++= Seq(
    "org.scalacheck" %% "scalacheck" % scalacheckVersion % "test",
    "org.scalatest" %% "scalatest" % scalatestVersion % "test"
  ),
  parallelExecution in Test := true,
  homepage := Some(url("https://github.com/twitter/bijection")),
  licenses += "Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"),
  scmInfo := Some(
    ScmInfo(
      browseUrl = url("https://github.com/twitter/bijection"),
      connection = "scm:git:git@github.com:twitter/bijection.git",
      devConnection = Some("scm:git@github.com:twitter/bijection.git")
    )
  ),
  developers ++= List(
    Developer(
      id = "oscar",
      name = "Oscar Boykin",
      email = "",
      url = url("http://twitter.com/posco")
    ),
    Developer(
      id = "marius",
      name = "Marius Eriksen",
      email = "",
      url = url("http://twitter.com/marius")
    ),
    Developer(
      id = "sritchie",
      name = "Sam Ritchie",
      email = "",
      url = url("http://twitter.com/sritchie")
    )
  )
)

val sharedSettings = Seq(
  // Publishing options:
  publishTo := Some {
    if (version.value.trim.endsWith("SNAPSHOT")) Opts.resolver.sonatypeSnapshots
    else Opts.resolver.sonatypeStaging
  },
  releaseCrossBuild := true,
  releasePublishArtifactsAction := (PgpKeys.publishSigned in ThisProject).value,
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
  OsgiKeys.importPackage ++= Seq(
    s"""scala.*;version="$$<range;[==,=+);${scalaVersion.value}>"""",
    "com.twitter.bijection.*;version=\"[${Bundle-Version}, ${Bundle-Version}]\"",
    "*"
  ),
  OsgiKeys.additionalHeaders := Map("-removeheaders" -> "Include-Resource,Private-Package")
)

val unreleasedModules = Set[String]()

// This returns the youngest jar we released that is compatible with the current
def youngestForwardCompatible(subProj: String) = {
  Some(subProj)
    .filterNot(unreleasedModules.contains(_))
    .map { s =>
      "com.twitter" %% ("bijection-" + s) % "0.9.6"
    }
    .toSet
}

/**
  * Generic AnyVal's were incorrectly reported as the underlying type in
  * the signature, while they were in fact instantiated AnyVal wrappers.
  *
  * This changed the reported binary signature, but not the actual binary
  * signature. This is source-breaking against java, but not binary
  * breaking
  * see https://github.com/scala/scala/pull/8127 for more details
  */
val changedSignatures8127 = List(
  "Base64String.unwrap",
  "Bijection.bytes2Base64",
  "Bijection.bytes2GZippedBase64",
  "Bijection.bytes2GzippedBytes",
  "BinaryBijections.bytes2Base64",
  "BinaryBijections.bytes2GZippedBase64",
  "BinaryBijections.bytes2GzippedBytes",
  "EnglishInt.bijectionToInt",
  "GZippedBase64String.unwrap",
  "GZippedBytes.andThen",
  "GZippedBytes.compose",
  "Injection.string2UrlEncodedString",
  "NumberSystems.binary",
  "NumberSystems.hexadecimal",
  "NumberSystems.octal",
  "StringCodec.string2UrlEncodedString",
  "StringInjections.string2UrlEncodedString",
  "guava.GuavaBinaryBijections.bytes2Base16",
  "guava.GuavaBinaryBijections.bytes2Base32",
  "guava.GuavaBinaryBijections.bytes2Base32HEX",
  "guava.GuavaBinaryBijections.bytes2Base64",
  "guava.GuavaBinaryBijections.bytes2Base64Url",
  "json.JsonNodeInjection.unparsed",
  "json.UnparsedJson.injection",
  "json.UnparsedJson.unwrap"
).map(part => s"com.twitter.bijection.$part")

/**
  * Empty this each time we publish a new version (and bump the minor number)
  */
val ignoredABIProblems = {
  import com.typesafe.tools.mima.core._
  import com.typesafe.tools.mima.core.ProblemFilters._
  Seq(
    exclude[ReversedMissingMethodProblem](
      "com.twitter.bijection.GeneratedTupleBufferable.tuple1"
    ),
    exclude[ReversedMissingMethodProblem](
      "com.twitter.bijection.twitter_util.UtilBijections.twitter2JavaFutureBijection"
    ),
    exclude[ReversedMissingMethodProblem](
      "com.twitter.bijection.twitter_util.UtilBijections.twitter2JavaFutureInjection"
    ),
    exclude[DirectMissingMethodProblem](
      "com.twitter.bijection.netty.ChannelBufferBijection.invert"
    ),
    exclude[DirectMissingMethodProblem](
      "com.twitter.bijection.Bijection.trav2Vector"
    )
  ) ++ changedSignatures8127.map(exclude[IncompatibleSignatureProblem])
}

def osgiExportAll(packs: String*) = {
  OsgiKeys.exportPackage := packs.map(_ + ".*;version=${Bundle-Version}")
}

lazy val bijection = {
  Project(
    id = "bijection",
    base = file(".")
  ).enablePlugins(SbtOsgi)
    .settings(
      buildLevelSettings
    )
    .settings(
      sharedSettings
    )
    .settings(
      mimaFailOnNoPrevious := false,
      test := {},
      publish := {}, // skip publishing for this root project.
      publishLocal := {}
    )
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
}

def module(name: String) = {
  val id = s"bijection-$name"
  Project(id = id, base = file(id))
    .enablePlugins(SbtOsgi)
    .settings(buildLevelSettings ++ sharedSettings)
    .settings(
      mimaPreviousArtifacts := youngestForwardCompatible(name),
      mimaBinaryIssueFilters ++= ignoredABIProblems
    )
}

/** No dependencies in bijection other than java + scala */
lazy val bijectionCore = {
  module("core").settings(
    osgiExportAll("com.twitter.bijection"),
    libraryDependencies ++= Seq(
      "com.novocode" % "junit-interface" % "0.11" % "test"
    ),
    sourceGenerators in Compile += Def.task {
      val main = (sourceManaged in Compile).value
      val out = streams.value
      val pkg = main / "scala" / "com" / "twitter" / "bijection"
      def genSrc(name: String, gen: => String) = {
        val srcFile = pkg / name
        IO.write(srcFile, gen)
        out.log.debug(s"generated $srcFile")
        srcFile
      }
      Seq(
        genSrc("GeneratedTupleBijections.scala", Generator.generate),
        genSrc("GeneratedTupleBuffer.scala", BufferableGenerator.generate)
      )
    }.taskValue
  )
}

lazy val bijectionProtobuf = {
  module("protobuf")
    .settings(
      osgiExportAll("com.twitter.bijection.protobuf"),
      libraryDependencies ++= Seq(
        "com.google.protobuf" % "protobuf-java" % "2.4.1"
      )
    )
    .dependsOn(
      bijectionCore % "test->test;compile->compile"
    )
}

val jsonParser = "org.codehaus.jackson" % "jackson-mapper-asl" % "1.9.11"

lazy val bijectionThrift = {
  module("thrift")
    .settings(
      osgiExportAll("com.twitter.bijection.thrift"),
      libraryDependencies ++= Seq(
        "org.apache.thrift" % "libthrift" % "0.6.1" exclude ("junit", "junit"),
        jsonParser
      )
    )
    .dependsOn(
      bijectionCore % "test->test;compile->compile",
      bijectionMacros
    )
}

lazy val bijectionGuava = {
  module("guava")
    .settings(
      osgiExportAll("com.twitter.bijection.guava"),
      libraryDependencies ++= Seq(
        // This dependency is required because scalac needs access to all java
        // runtime annotations even though javac does not as detailed here:
        // http://code.google.com/p/guava-libraries/issues/detail?id=1095
        "com.google.code.findbugs" % "jsr305" % "3.0.2",
        "com.google.guava" % "guava" % "14.0.1"
      )
    )
    .dependsOn(
      bijectionCore % "test->test;compile->compile"
    )
}

lazy val bijectionScrooge = {
  module("scrooge")
    .settings(
      osgiExportAll("com.twitter.bijection.scrooge"),
      libraryDependencies ++= Seq(
        "org.apache.thrift" % "libthrift" % "0.6.1" exclude ("junit", "junit"),
        scroogeSerializer,
        util("core"),
        finagle("core")
      )
    )
    .dependsOn(
      bijectionCore % "test->test;compile->compile",
      bijectionMacros,
      bijectionThrift
    )
}

lazy val bijectionJson = {
  module("json")
    .settings(
      osgiExportAll("com.twitter.bijection.json"),
      libraryDependencies += jsonParser
    )
    .dependsOn(
      bijectionCore % "test->test;compile->compile"
    )
}

lazy val bijectionUtil = {
  module("util")
    .settings(
      osgiExportAll("com.twitter.bijection.twitter_util"),
      libraryDependencies += util("core")
    )
    .dependsOn(
      bijectionCore % "test->test;compile->compile"
    )
}

lazy val bijectionFinagleMySql = {
  module("finagle-mysql")
    .settings(
      osgiExportAll("com.twitter.bijection.finagle_mysql"),
      libraryDependencies ++= Seq(
        finagle("mysql"),
        util("core")
      )
    )
    .dependsOn(
      bijectionCore % "test->test;compile->compile"
    )
}

lazy val bijectionClojure = {
  module("clojure")
    .settings(
      osgiExportAll("com.twitter.bijection.clojure"),
      libraryDependencies += "org.clojure" % "clojure" % "1.10.1"
    )
    .dependsOn(
      bijectionCore % "test->test;compile->compile"
    )
}

lazy val bijectionNetty = {
  module("netty")
    .settings(
      osgiExportAll("com.twitter.bijection.netty"),
      libraryDependencies += "io.netty" % "netty" % "3.5.13.Final"
    )
    .dependsOn(
      bijectionCore % "test->test;compile->compile"
    )
}

lazy val bijectionAvro = {
  module("avro")
    .settings(
      osgiExportAll("com.twitter.bijection.avro"),
      libraryDependencies ++= Seq(
        "org.apache.avro" % "avro" % "1.7.7"
      )
    )
    .dependsOn(
      bijectionCore % "test->test;compile->compile"
    )
}

lazy val bijectionHbase = {
  module("hbase")
    .settings(
      osgiExportAll("com.twitter.bijection.hbase"),
      libraryDependencies ++= Seq(
        "org.apache.hbase" % "hbase" % "0.94.27" % "provided->default" exclude ("org.jruby", "jruby-complete"),
        "org.apache.hadoop" % "hadoop-core" % "1.2.1" % "provided->default"
      )
    )
    .dependsOn(
      bijectionCore % "test->test;compile->compile",
      bijectionMacros % "compile->compile"
    )
}

lazy val bijectionJodaTime = {
  module("jodatime")
    .settings(
      osgiExportAll("com.twitter.bijection.jodatime"),
      libraryDependencies ++= Seq(
        "joda-time" % "joda-time" % "2.10.4",
        "org.joda" % "joda-convert" % "2.2.1"
      )
    )
    .dependsOn(
      bijectionCore % "test->test;compile->compile"
    )
}

lazy val bijectionJson4s = {
  module("json4s")
    .settings(
      osgiExportAll("com.twitter.bijection.json4s"),
      libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-reflect" % scalaVersion.value,
        "org.json4s" %% "json4s-native" % "3.6.7",
        "org.json4s" %% "json4s-ext" % "3.6.7"
      )
    )
    .dependsOn(
      bijectionCore % "test->test;compile->compile"
    )
}

lazy val bijectionMacros = {
  module("macros")
    .settings(
      libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-library" % scalaVersion.value,
        "org.scala-lang" % "scala-reflect" % scalaVersion.value
      ),
      addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
    )
    .dependsOn(
      bijectionCore
    )
}
