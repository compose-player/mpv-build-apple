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
  dependency = Dependency.lcms2,
  targets = DEFAULT_TARGETS,
  build = { arguments = arrayOf() }
)


