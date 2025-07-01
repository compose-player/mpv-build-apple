package fr.composeplayer.builds.apple.tasks

import fr.composeplayer.builds.apple.misc.BuildTarget
import fr.composeplayer.builds.apple.misc.Dependency
import fr.composeplayer.builds.apple.misc.cmakeSystemName
import fr.composeplayer.builds.apple.misc.sdk
import fr.composeplayer.builds.apple.utils.CommandScope
import fr.composeplayer.builds.apple.utils.execExpectingResult
import fr.composeplayer.builds.apple.utils.execExpectingSuccess
import fr.composeplayer.builds.apple.utils.exists
import fr.composeplayer.builds.apple.utils.parallelism
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

var AutoBuildTask.args: List<String>
  get() = this.arguments.get().toList()
  set(value) { this.arguments.set(value.toTypedArray()) }

abstract class AutoBuildTask : DefaultTask() {

  @get:Input
  @get:Optional
  abstract val skip: Property<Boolean>

  @get:Input abstract val buildTarget: Property<BuildTarget>
  @get:Input abstract val dependency: Property<Dependency>
  @get:Input abstract val arguments: Property<Array<String>>

  private val environement: Map<String, String?> by lazy {
    val context = buildContext(dependency.get(), buildTarget.get())
    val cFlags = context.cFlags.joinToString(separator = " ")
    val ldFlags = context.ldFlags.joinToString(separator = " ")
    val pkgConfigPath = let {
      val list = buildList {
        for (dep in Dependency.values()) {
          val _context = buildContext(dep, context.buildTarget)
          val pkgConfig = _context.prefixDir.resolve("lib/pkgconfig")
          add(pkgConfig.absolutePath)
        }
      }
      list.joinToString(":")
    }
    val pkgConfigPathDefault = execExpectingResult { command = arrayOf("pkg-config", "--variable", "pc_path", "pkg-config") }
    mutableMapOf(
      "LC_CTYPE" to  "C",
      "CC" to  "/usr/bin/clang",
      "CXX" to  "/usr/bin/clang++",
      "CURRENT_ARCH" to  context.buildTarget.arch.name,
      "CFLAGS" to cFlags,
      "CPPFLAGS" to cFlags,
      "CXXFLAGS" to cFlags,
      "LDFLAGS" to ldFlags,
      "PKG_CONFIG_LIBDIR" to "${pkgConfigPath}:${pkgConfigPathDefault}",
      //"PKG_CONFIG_PATH" to pkgConfigPath.absolutePath,
    )
  }


  @TaskAction
  fun execute() {
    if (skip.isPresent && skip.get()) return
    val context = buildContext(dependency.get(), buildTarget.get())
    val meson = context.sourceDir.resolve("meson.build")
    val autogen = context.sourceDir.resolve("autogen")
    val cMakeLists = context.sourceDir.resolve("CMakeLists.txt")
    val configure = context.sourceDir.resolve("configure")
    val bootstrap = context.sourceDir.resolve("bootstrap")

    if (!context.sourceDir.exists) throw GradleException("No source found for component $dependency")

    try {
      context.buildDir.mkdirs()
      context.prefixDir.mkdirs()

      val crossfile = CrossFileCreator(context).create()

      when {
        meson.exists() -> {
          logger.info("Building component [$dependency] with meson")
          execExpectingSuccess {
            env.applyFrom(environement)
            workingDir = context.sourceDir
            command = arrayOf(
              "meson", "setup", context.buildDir.absolutePath,
              "--default-library=both",
              "--cross-file", crossfile.absolutePath,
              *arguments.get(),
            )
          }
          execExpectingSuccess {
            env.applyFrom(environement)
            workingDir = context.buildDir
            command = arrayOf("meson", "compile", "--clean")
          }
          execExpectingSuccess {
            env.applyFrom(environement)
            workingDir = context.buildDir
            command = arrayOf("meson", "compile", "--verbose")
          }
          execExpectingSuccess {
            env.applyFrom(environement)
            workingDir = context.buildDir
            command = arrayOf("meson", "install")
          }
        }
        else -> {
          if (autogen.exists()) {
            logger.info("Running autogen for component [$dependency]")
            execExpectingSuccess {
              env.applyFrom(environement)
              env["NOCONFIGURE"] = "1"
              workingDir = context.sourceDir
              command = arrayOf(autogen.absolutePath)
            }
          }
          when {
            cMakeLists.exists() -> {
              logger.info("Runing cmake for component [$dependency]")
              execExpectingSuccess {
                env.applyFrom(environement)
                workingDir = context.buildDir
                command = arrayOf(
                  "cmake", context.sourceDir.absolutePath,
                  "-DCMAKE_VERBOSE_MAKEFILE=0",
                  "-DCMAKE_BUILD_TYPE=Release",
                  "-DCMAKE_OSX_SYSROOT=${buildTarget.get().platform.sdk.lowercase()}",
                  "-DCMAKE_OSX_ARCHITECTURES=${buildTarget.get().arch.name}",
                  "-DCMAKE_SYSTEM_NAME=${buildTarget.get().platform.cmakeSystemName}",
                  "-DCMAKE_SYSTEM_PROCESSOR=${buildTarget.get().arch.name}",
                  "-DCMAKE_INSTALL_PREFIX=${context.prefixDir.absolutePath}",
                  "-DBUILD_SHARED_LIBS=ON",
                  "-DCMAKE_POLICY_VERSION_MINIMUM=3.5",
                  *arguments.get(),
                )
              }
            }
            else -> {
              if (!configure.exists() && bootstrap.exists()) {
                logger.info("Runing bootstrap for component [$dependency]")
                execExpectingSuccess {
                  env.applyFrom(environement)
                  workingDir = context.sourceDir
                  command = arrayOf(bootstrap.absolutePath)
                }
              }
              if (!configure.exists()) {
                throw GradleException("No build system found for dependency: ${dependency}")
              }
              logger.info("Runing configure for component [$dependency]")
              execExpectingSuccess {
                env.applyFrom(environement)
                workingDir = context.buildDir
                command = arrayOf(
                  configure.absolutePath,
                  "--prefix=${context.prefixDir.absolutePath}",
                  *arguments.get(),
                )
              }
            }
          }
          logger.info("Runing make for component [$dependency]")
          execExpectingSuccess {
            env.applyFrom(environement)
            workingDir = context.buildDir
            command = arrayOf("make", "-j$parallelism", "V=1")
          }
          execExpectingSuccess {
            env.applyFrom(environement)
            workingDir = context.buildDir
            command = arrayOf("make", "-j$parallelism", "V=1", "install")
          }
        }
      }

    } catch (error: Throwable) {
     context.buildDir.deleteRecursively()
     context.prefixDir.deleteRecursively()
    }
  }

}

fun CommandScope.Environment.applyFrom(map: Map<String, String?>) {
  for (entry in map) this[entry.key] = entry.value
}