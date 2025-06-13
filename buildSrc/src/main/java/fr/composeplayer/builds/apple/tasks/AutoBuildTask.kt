package fr.composeplayer.builds.apple.tasks

import fr.composeplayer.builds.apple.misc.Dependency
import fr.composeplayer.builds.apple.misc.Platform
import fr.composeplayer.builds.apple.misc.cmakeSystemName
import fr.composeplayer.builds.apple.misc.requirements
import fr.composeplayer.builds.apple.misc.sdk
import fr.composeplayer.builds.apple.utils.execExpectingSuccess
import fr.composeplayer.builds.apple.utils.parallelism
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

open class AutoBuildTask : DefaultTask() {

  @Input lateinit var platform: Platform
  @Input lateinit var dependency: Dependency
  @Input lateinit var arguments: Array<String>

  @TaskAction
  fun execute() {
    println("AutoBuildTask.execute")
    val context = buildContext(dependency, platform)
    val meson = context.sourceDir.resolve("build.meson")
    val waf = context.sourceDir.resolve("waf")
    val autogen = context.sourceDir.resolve("autogen")
    val cMakeLists = context.sourceDir.resolve("CMakeLists.txt")
    val configure = context.sourceDir.resolve("configure")
    val bootstrap = context.sourceDir.resolve("bootstrap")

    for (dep in dependency.requirements) {
      val c = buildContext(dep, platform)
      if ( c.buildDir.exists() ) continue
      throw GradleException("Component [$dependency] requires [$dep] to be built before")
    }

    context.buildDir.mkdirs()
    context.prefixDir.mkdirs()

    when {
      meson.exists() -> {
        val crossfile = CrossFileCreator(context).create()
        execExpectingSuccess {
          workingDir = context.sourceDir
          command = arrayOf(
            "meson", "setup", context.buildDir.absolutePath,
            "--default-library=both",
            "--cross-file", crossfile.absolutePath,
            *arguments,
          )
        }
        execExpectingSuccess {
          workingDir = context.buildDir
          command = arrayOf("meson", "compile", "--clean")
        }
        execExpectingSuccess {
          workingDir = context.buildDir
          command = arrayOf("meson", "compile", "--verbose")
        }
        execExpectingSuccess {
          workingDir = context.buildDir
          command = arrayOf("meson", "install")
        }
      }
      else -> {
        if (autogen.exists()) {
          execExpectingSuccess {
            env["NOCONFIGURE"] = "1"
            workingDir = context.sourceDir
            command = arrayOf(autogen.absolutePath)
          }
        }
        when {
          cMakeLists.exists() -> {
            println("context.buildDir = ${context.buildDir}")
            println("context.sourceDir = ${context.sourceDir}")
            execExpectingSuccess {
              workingDir = context.buildDir
              command = arrayOf(
                "cmake", context.sourceDir.absolutePath,
                "-DCMAKE_VERBOSE_MAKEFILE=0",
                "-DCMAKE_BUILD_TYPE=Release",
                "-DCMAKE_OSX_SYSROOT=${platform.sdk.lowercase()}",
                "-DCMAKE_OSX_ARCHITECTURES=${platform.arch.name}",
                "-DCMAKE_SYSTEM_NAME=${platform.cmakeSystemName}",
                "-DCMAKE_SYSTEM_PROCESSOR=${platform.arch.name}",
                "-DCMAKE_INSTALL_PREFIX=${context.prefixDir.absolutePath}",
                "-DBUILD_SHARED_LIBS=ON",
                *arguments,
              )
            }
          }
          else -> {
            if (!configure.exists() && bootstrap.exists()) {
              execExpectingSuccess {
                workingDir = context.sourceDir
                command = arrayOf(bootstrap.absolutePath)
              }
            }
            if (!configure.exists()) {
              throw GradleException("No build system found for dependency: ${dependency.name}")
            }
            execExpectingSuccess {
              workingDir = context.buildDir
              command = arrayOf(
                configure.absolutePath,
                "--prefix=${context.prefixDir.absolutePath}",
                *arguments,
              )
            }
          }
        }
        execExpectingSuccess {
          workingDir = context.buildDir
          command = arrayOf("make", "-j$parallelism")
        }
        execExpectingSuccess {
          workingDir = context.buildDir
          command = arrayOf("make", "-j$parallelism", "install")
        }
      }
    }
  }

}