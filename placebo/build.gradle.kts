import fr.composeplayer.builds.apple.misc.Dependency
import fr.composeplayer.builds.apple.tasks.CreateFramework
import fr.composeplayer.builds.apple.utils.BUILD_VERSION
import fr.composeplayer.builds.apple.utils.DEFAULT_TARGETS
import fr.composeplayer.builds.apple.utils.execExpectingSuccess
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
  createFramework = {
    doLast {
      if (type == CreateFramework.FrameworkType.static) return@doLast
      execExpectingSuccess {
        workingDir = project.rootDir.resolve("fat-frameworks/shared/$platform/Placebo.framework")
        command = arrayOf(
          "install_name_tool", "-change", "@rpath/libshaderc_shared.1.dylib", "@rpath/Shaderc_combined.framework/Shaderc_combined", "Placebo"
        )
      }
    }
  }
)
