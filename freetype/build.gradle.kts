import fr.composeplayer.builds.apple.misc.Dependency
import fr.composeplayer.builds.apple.utils.BUILD_VERSION
import fr.composeplayer.builds.apple.utils.DEFAULT_TARGETS
import fr.composeplayer.builds.apple.utils.registerBasicWorkflow

plugins {
  kotlin("jvm")
}

group = "fr.composeplayer.builds.mpv"
version = BUILD_VERSION

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


