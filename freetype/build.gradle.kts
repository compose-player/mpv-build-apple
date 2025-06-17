import fr.composeplayer.builds.apple.misc.Dependency
import fr.composeplayer.builds.apple.utils.DEFAULT_TARGETS
import fr.composeplayer.builds.apple.utils.registerBasicWorkflow

plugins {
  kotlin("jvm")
}

group = "fr.composeplayer.builds.mpv"
version = libs.versions.library

repositories { mavenCentral() }
kotlin { jvmToolchain(23) }

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


