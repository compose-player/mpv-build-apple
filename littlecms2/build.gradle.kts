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

afterEvaluate {

  registerBasicWorkflow(
    dependency = Dependency.lcms,
    targets = DEFAULT_TARGETS,
    build = { arguments = arrayOf() }
  )

}


