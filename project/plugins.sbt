resolvers ++= Seq(
  "jgit-repo" at "http://download.eclipse.org/jgit/maven"
)

addSbtPlugin("com.github.gseitz" % "sbt-release"     % "1.0.6")
addSbtPlugin("com.eed3si9n"      % "sbt-doge"        % "0.1.5")
addSbtPlugin("com.jsuereth"      % "sbt-pgp"         % "1.1.0")
addSbtPlugin("com.typesafe"      % "sbt-mima-plugin" % "0.1.17")
addSbtPlugin("com.typesafe.sbt"  % "sbt-ghpages"     % "0.5.4")
addSbtPlugin("com.typesafe.sbt"  % "sbt-osgi"        % "0.8.0")
addSbtPlugin("org.scoverage"     % "sbt-scoverage"   % "1.5.0")
addSbtPlugin("org.xerial.sbt"    % "sbt-sonatype"    % "1.1")
