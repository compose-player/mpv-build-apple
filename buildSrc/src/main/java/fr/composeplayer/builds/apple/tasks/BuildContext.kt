package fr.composeplayer.builds.apple.tasks

import fr.composeplayer.builds.apple.misc.Dependency
import fr.composeplayer.builds.apple.misc.*
import fr.composeplayer.builds.apple.utils.cFlags
import fr.composeplayer.builds.apple.utils.ldFlags
import org.gradle.api.Project
import org.gradle.api.Task
import java.io.File

fun Task.buildContext(dep: Dependency, target: BuildTarget): BuildContext {
  return BuildContext(project, dep, target)
}

class BuildContext(
  val project: Project,
  val dependency: Dependency,
  val buildTarget: BuildTarget,
) {
  val sourceDir = File(project.rootDir, "vendor/${dependency.name}")
  val buildDir = File(project.rootDir, "builds/${buildTarget.platform.name}/${buildTarget.arch.name}/${dependency.name}/")
  val prefixDir = File(project.rootDir, "binaries/${buildTarget.platform.name}/${buildTarget.arch.name}")
  operator fun component1(): File = sourceDir
  operator fun component2(): File = buildDir

  val cFlags: List<String>
    get() = buildList {
      addAll(buildTarget.cFlags)
      for (dep in dependency.flagsDependencelibrarys) {
        val includeDir = prefixDir.resolve("include")
        if ( !includeDir.exists() ) continue
        add("-I${includeDir.absolutePath}")
      }
    }

  val ldFlags: List<String>
    get() = buildList {
      addAll(buildTarget.ldFlags)
      for (dep in dependency.flagsDependencelibrarys) {
        val name = dep.removePrefix("lib").removePrefix("Lib")
        val libDir = prefixDir.resolve("lib")
        if ( !libDir.exists() ) continue
        add("-L${libDir.absolutePath}")
        add("-l${name}")
      }
    }

}