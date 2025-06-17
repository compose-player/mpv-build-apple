import fr.composeplayer.builds.apple.misc.Dependency
import fr.composeplayer.builds.apple.misc.rustTarget
import fr.composeplayer.builds.apple.tasks.buildContext
import fr.composeplayer.builds.apple.utils.DEFAULT_TARGETS
import fr.composeplayer.builds.apple.utils.execExpectingSuccess
import fr.composeplayer.builds.apple.utils.registerBasicWorkflow

plugins {
  kotlin("jvm")
}

group = "fr.composeplayer.builds.mpv"
version = libs.versions.library

repositories { mavenCentral() }
kotlin { jvmToolchain(23) }

afterEvaluate {

  val dependency = Dependency.dovi

  registerBasicWorkflow(
    targets = DEFAULT_TARGETS,
    dependency = dependency,
    build = {
      skip = true
      arguments = arrayOf()
      val context = buildContext(dependency, buildTarget.get())
      doLast {
        execExpectingSuccess {
          val target = context.buildTarget.rustTarget.let {
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
    },
  )

}


