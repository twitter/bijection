name := "bijection-thrift"

version := "0.1.0-SNAPSHOT"

resolvers ++= Seq(
  "snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "releases"  at "http://oss.sonatype.org/content/repositories/releases"
)

libraryDependencies ++= Seq(
  "org.apache.thrift" % "libthrift" % "0.6.1" exclude("junit", "junit"),
  "org.codehaus.jackson" % "jackson-mapper-asl" % "1.8.1",
  "org.scalacheck" %% "scalacheck" % "1.10.0" % "test",
  "org.scala-tools.testing" % "specs_2.9.0-1" % "1.6.8" % "test"
)

parallelExecution in Test := true

// Publishing options:

publishMavenStyle := false

publishArtifact in Test := false

pomIncludeRepository := { x => false }

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("sonatype-snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("sonatype-releases"  at nexus + "service/local/staging/deploy/maven2")
}

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
    <url>git@github.com:twitter/scalding.git</url>
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
