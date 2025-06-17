import fr.composeplayer.builds.apple.misc.Dependency
import fr.composeplayer.builds.apple.misc.host
import fr.composeplayer.builds.apple.tasks.buildContext
import fr.composeplayer.builds.apple.utils.DEFAULT_TARGETS
import fr.composeplayer.builds.apple.utils.execExpectingSuccess
import fr.composeplayer.builds.apple.utils.registerBasicWorkflow
import java.nio.file.Files

plugins {
  kotlin("jvm")
}

group = "fr.composeplayer.builds.mpv"
version = libs.versions.library

repositories { mavenCentral() }
kotlin { jvmToolchain(23) }

registerBasicWorkflow(
  targets = DEFAULT_TARGETS,
  dependency = Dependency.shaderc,
  prebuild = { target ->
    val context = buildContext(Dependency.shaderc, target)
    enabled = !context.sourceDir.resolve("third_party/spirv-tools").exists()
    doLast {
      execExpectingSuccess {
        workingDir = context.sourceDir.resolve("utils")
        command = arrayOf("python3", "git-sync-deps")
      }
      val reduce = context.sourceDir.resolve("third_party/spirv-tools/tools/reduce/reduce.cpp")
      val fuzz = context.sourceDir.resolve("third_party/spirv-tools/tools/fuzz/fuzz.cpp")
      Files.write(
        /* path = */ reduce.toPath(),
        /* lines = */ reduce.readLines().toMutableList().also { lines ->
          lines[36] = """FILE* fp = popen(nullptr, "r");"""
          lines[41] = "return fp == NULL;"
        }
      )
      Files.write(
        /* path = */ fuzz.toPath(),
        /* lines = */ fuzz.readLines().toMutableList().also { lines ->
          lines[47] = """FILE* fp = popen(nullptr, "r");"""
          lines[52] = "return fp == NULL;"
        }
      )
    }
  },
  build = {
    this.arguments = arrayOf(
      "-DSHADERC_SKIP_TESTS=ON",
      "-DSHADERC_SKIP_EXAMPLES=ON",
      "-DSHADERC_SKIP_COPYRIGHT_CHECK=ON",
      "-DENABLE_EXCEPTIONS=ON",
      "-DENABLE_GLSLANG_BINARIES=OFF",
      "-DSPIRV_SKIP_EXECUTABLES=ON",
      "-DSPIRV_TOOLS_BUILD_STATIC=ON",
      "-DBUILD_SHARED_LIBS=ON",
    )
  }
)
