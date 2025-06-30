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
  dependency = Dependency.dav1d,
  targets = DEFAULT_TARGETS,
  build = {
    this.arguments = arrayOf(
      "-Denable_asm=false",
      "-Denable_tests=false",
      "-Denable_tools=false",
      "-Denable_examples=false",
      "-Dxxhash_muxer=disabled",
    )
  }
)

