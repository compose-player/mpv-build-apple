import fr.composeplayer.builds.apple.misc.Architecture
import fr.composeplayer.builds.apple.misc.Dependency
import fr.composeplayer.builds.apple.misc.Platform
import fr.composeplayer.builds.apple.misc.name
import fr.composeplayer.builds.apple.misc.rustTarget
import fr.composeplayer.builds.apple.tasks.CloneTask
import fr.composeplayer.builds.apple.tasks.applyFrom
import fr.composeplayer.builds.apple.tasks.buildContext
import fr.composeplayer.builds.apple.utils.execExpectingSuccess

plugins {
  kotlin("jvm")
}

group = "fr.composeplayer.builds.mpv"
version = libs.versions.library

repositories { mavenCentral() }
kotlin { jvmToolchain(23) }

afterEvaluate {

  val platforms = listOf(
    Platform.MacOS(Architecture.arm64),
    Platform.MacOS(Architecture.x86_64),
    Platform.IOS(Architecture.arm64),
  )

  tasks.getByName("clean") {
    doLast {
      val file = File(rootProject.rootDir, "vendor/${Dependency.dovi}")
      if (file.exists()) file.deleteRecursively()
    }
  }

  tasks.register<CloneTask>("clone") {
    applyFrom(Dependency.dovi)
  }

  val buildAll by tasks.register<Task>("buildAll")

  for (platform in platforms) {

    tasks.register<DefaultTask>(
      name = "build[${platform.name}][${platform.arch.name}]",
    ) {
      buildAll.dependsOn(this)
      val context = buildContext(Dependency.dovi, platform)
      doLast {
        execExpectingSuccess {
          val target = platform.rustTarget.let {
            when (it) {
              "x86_64-apple-ios-sim" -> "x86_64-apple-ios"
              else -> it
            }
          }
          workingDir = context.sourceDir.resolve("dolby_vision")
          command = arrayOf(
            "cargo", "+nightly", "cinstall",
            "-Zbuild-std=std,panic_abort",
            "--release",
            "--prefix=${context.prefixDir.absolutePath}",
            "--target=$target"
          )
        }
      }

    }
  }



}


