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
  dependency = Dependency.placebo,
  build = {
    this.arguments = arrayOf(
      "-Dopengl=enabled",
      "-Dvulkan=enabled",
      "-Dshaderc=enabled",
      "-Dlcms=enabled",
      "-Dxxhash=disabled",
      "-Dunwind=disabled",
      "-Dglslang=disabled",
      "-Dd3d11=disabled",
      "-Ddemos=false",
      "-Dtests=false",
      "-Ddovi=enabled", "-Dlibdovi=enabled"
    )
  },
)
