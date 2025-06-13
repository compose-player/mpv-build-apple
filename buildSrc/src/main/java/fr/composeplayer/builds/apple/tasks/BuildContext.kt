package fr.composeplayer.builds.apple.tasks

import fr.composeplayer.builds.apple.misc.Dependency
import fr.composeplayer.builds.apple.misc.Platform
import fr.composeplayer.builds.apple.misc.*
import org.gradle.api.Project
import org.gradle.api.Task
import java.io.File

fun Task.buildContext(dep: Dependency, platform: Platform): BuildContext {
  return BuildContext(project, dep, platform)
}

class BuildContext(
  val project: Project,
  val dependency: Dependency,
  val platform: Platform,
) {
  val sourceDir = File(project.rootDir, "vendor/${dependency.name}")
  val buildDir = File(project.rootDir, "builds/${platform.name}/${platform.arch.name}/${dependency.name}/")
  val prefixDir = File(project.rootDir, "binaries/${platform.name}/${platform.arch.name}")
  operator fun component1(): File = sourceDir
  operator fun component2(): File = buildDir
}