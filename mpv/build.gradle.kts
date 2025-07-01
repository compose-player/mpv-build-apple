import fr.composeplayer.builds.apple.misc.Dependency
import fr.composeplayer.builds.apple.misc.Platform
import fr.composeplayer.builds.apple.tasks.CreateFramework
import fr.composeplayer.builds.apple.tasks.args
import fr.composeplayer.builds.apple.utils.*

plugins {
  kotlin("jvm")
}

group = "fr.composeplayer.builds.mpv"
version = BUILD_VERSION

repositories { mavenCentral() }
kotlin { jvmToolchain(23) }

registerBasicWorkflow(
  targets = DEFAULT_TARGETS,
  dependency = Dependency.mpv,
  build = {
    this.args = buildList {
      add(
        "-Dlibmpv=true",
        "-Dgl=enabled",
        "-Dplain-gl=enabled",
        "-Diconv=enabled",
        "-Duchardet=enabled",
        "-Dvulkan=enabled",
        "-Dmoltenvk=enabled",
        "-Djavascript=disabled",
        "-Dzimg=disabled",
        "-Djpeg=disabled",
        "-Dvapoursynth=disabled",
        "-Drubberband=disabled",

        "-Dgpl=false",
        "-Dcplayer=false",
        "-Dlibbluray=disabled",
        "-Dlua=disabled",
        "-Dlibarchive=disabled",
      )
      if (buildTarget.get().platform == Platform.macos) {
        add(
          "-Dswift-flags='-sdk ${buildTarget.get().platform.isysroot} -target ${buildTarget.get().deploymentTarget}'",
          "-Dswift-build=enabled",
          "-Dcocoa=enabled",
          "-Dcoreaudio=enabled",
          "-Davfoundation=enabled",
          "-Dgl-cocoa=enabled",
          "-Dvideotoolbox-gl=enabled",
        )
      } else {
        add(
          "-Dvideotoolbox-gl=disabled",
          "-Dswift-build=disabled",
          "-Daudiounit=enabled",
          "-Davfoundation=disabled",
          "-Dios-gl=enabled",
        )
      }
    }
  },
  createFramework = {
    doLast {
      if (type == CreateFramework.FrameworkType.static) return@doLast
      execExpectingSuccess {
        workingDir = project.rootDir.resolve("fat-frameworks/shared/$platform/Mpv.framework")
        command = arrayOf(
          "install_name_tool", "-change", "@rpath/libuchardet.0.dylib", "@rpath/Uchardet", "Mpv"
        )
      }
    }
  }
)
