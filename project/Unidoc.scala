package bijection

import sbt._
import sbt.Keys._
import sbt.Project.Initialize

/** Borrowed from https://github.com/akka/akka/blob/master/project/Unidoc.scala */
object Unidoc extends AutoPlugin {

  override def requires = plugins.JvmPlugin

  val unidocDirectory = SettingKey[File]("unidoc-directory")
  val unidocAllSources = TaskKey[Seq[File]]("unidoc-all-sources")
  val unidocSources = TaskKey[Seq[File]]("unidoc-sources")
  val unidocAllClasspaths = TaskKey[Seq[Classpath]]("unidoc-all-classpaths")
  val unidocClasspath = TaskKey[Seq[File]]("unidoc-classpath")
  val unidoc = TaskKey[File]("unidoc", "Create unified scaladoc for all aggregates")

  override def projectSettings = Seq(
    unidocDirectory := crossTarget.value / "unidoc",
    unidocAllSources := Def.taskDyn {
      val projectRef =  thisProjectRef.value
      val structure = buildStructure.value
      val projects = aggregated(projectRef, structure)
      val compositeTask = projects.map { proj =>
        (sources in (proj, Compile))
      }.join
      compositeTask
    }.value.flatten,
    unidocSources := unidocAllSources.value,
    unidocAllClasspaths := Def.taskDyn {
      val projectRef =  thisProjectRef.value
      val structure = buildStructure.value
      val projects = aggregated(projectRef, structure)
      val compositeTask = projects.map { proj =>
        (dependencyClasspath  in (proj, Compile))
      }.join
      compositeTask
    }.value,
    unidocClasspath := unidocAllClasspaths.value.flatten.map(_.data).distinct,
    unidoc := {
      val scaladoc = Doc.scaladoc("main", streams.value.cacheDirectory / "unidoc", compilers.value.scalac)
      scaladoc(
        unidocSources.value,
        unidocClasspath.value,
        unidocDirectory.value,
        (scalacOptions in doc).value,
        100,
        streams.value.log
      )
      unidocDirectory.value
    }
  )

  def aggregated(
    projectRef: ProjectRef,
    structure: BuildStructure
  ): Seq[ProjectRef] = {
    val maybeProject: Option[ResolvedProject] = Project.getProjectForReference(projectRef, structure)
    val aggregatedRefs: Seq[ProjectRef] = maybeProject.toSeq.flatMap(_.aggregate)
    aggregatedRefs flatMap { ref => ref +: aggregated(ref, structure) }
  }

}
