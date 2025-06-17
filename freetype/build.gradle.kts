import fr.composeplayer.builds.apple.misc.Architecture
import fr.composeplayer.builds.apple.misc.BuildTarget
import fr.composeplayer.builds.apple.misc.Dependency
import fr.composeplayer.builds.apple.misc.Platform
import fr.composeplayer.builds.apple.tasks.AutoBuildTask
import fr.composeplayer.builds.apple.tasks.CloneTask
import fr.composeplayer.builds.apple.tasks.applyFrom
import fr.composeplayer.builds.apple.utils.DEFAULT_TARGETS
import fr.composeplayer.builds.apple.utils.registerBasicWorkflow

plugins {
  kotlin("jvm")
}

group = "fr.composeplayer.builds.mpv"
version = libs.versions.library

repositories { mavenCentral() }
kotlin { jvmToolchain(23) }

afterEvaluate {

  registerBasicWorkflow(
    targets = DEFAULT_TARGETS,
    dependency = Dependency.freetype,
    build = {
      this.arguments = arrayOf(
        "-Dzlib=enabled",
        "-Dharfbuzz=disabled",
        "-Dbzip2=disabled",
        "-Dmmap=disabled",
        "-Dpng=disabled",
        "-Dbrotli=disabled",
      )
    },
  )


}


