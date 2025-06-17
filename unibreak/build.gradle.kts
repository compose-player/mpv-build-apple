import fr.composeplayer.builds.apple.misc.Dependency
import fr.composeplayer.builds.apple.misc.host
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
  dependency = Dependency.unibreak,
  build = {
    this.arguments = arrayOf(
      "--enable-static",
      "--enable-shared",
      "--disable-fast-install",
      "--disable-dependency-tracking",
      "--host=${buildTarget.get().host}",
    )
  },
)
