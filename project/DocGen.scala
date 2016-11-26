package bijection

import sbt._
import Keys._

import com.typesafe.sbt.git.GitRunner
import com.typesafe.sbt.SbtGit.GitKeys
import com.typesafe.sbt.SbtSite.{site, SiteKeys}
import com.typesafe.sbt.SbtGhPages.{ghpages, GhPagesKeys => ghkeys}
import com.typesafe.sbt.SbtGit.GitKeys.gitRemoteRepo

object DocGen extends AutoPlugin {

  override def requires = Unidoc

  val docDirectory = "target/site"
  val aggregateName = "bijection"

  def syncLocal = (ghkeys.updatedRepository, GitKeys.gitRunner, streams) map { (repo, git, s) =>
    cleanSite(repo, git, s) // First, remove 'stale' files.
    val rootPath = file(docDirectory) // Now copy files.
    IO.copyDirectory(rootPath, repo)
    IO.touch(repo / ".nojekyll")
    repo
  }

  private def cleanSite(dir: File, git: GitRunner, s: TaskStreams): Unit = {
    val toClean = IO.listFiles(dir).filterNot(_.getName == ".git").map(_.getAbsolutePath).toList
    if (!toClean.isEmpty)
      git(("rm" :: "-r" :: "-f" :: "--ignore-unmatch" :: toClean): _*)(dir, s.log)
    ()
  }

  def unidocSettings: Seq[sbt.Setting[_]] =
    site.includeScaladoc(docDirectory) ++ Seq(
      scalacOptions in doc <++= (version, baseDirectory in LocalProject(aggregateName)).map {
        (v, rootBase) =>
          val tagOrBranch = if (v.endsWith("-SNAPSHOT")) "develop" else v
          val docSourceUrl = "https://github.com/twitter/" + aggregateName + "/tree/" + tagOrBranch + "€{FILE_PATH}.scala"
          Seq("-sourcepath", rootBase.getAbsolutePath, "-doc-source-url", docSourceUrl)
      },
      Unidoc.unidocDirectory := file(docDirectory),
      gitRemoteRepo := "git@github.com:twitter/" + aggregateName + ".git",
      ghkeys.synchLocal <<= syncLocal
    )

  override def projectSettings = site.settings ++ ghpages.settings ++ unidocSettings

}
