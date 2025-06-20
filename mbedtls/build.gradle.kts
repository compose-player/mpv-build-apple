import fr.composeplayer.builds.apple.misc.Dependency
import fr.composeplayer.builds.apple.misc.frameworks
import fr.composeplayer.builds.apple.misc.versionName
import fr.composeplayer.builds.apple.tasks.CreateFramework
import fr.composeplayer.builds.apple.tasks.buildContext
import fr.composeplayer.builds.apple.utils.BUILD_VERSION
import fr.composeplayer.builds.apple.utils.DEFAULT_TARGETS
import fr.composeplayer.builds.apple.utils.execExpectingResult
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
  dependency = Dependency.mbedtls,
  build = {
    this.arguments = arrayOf(
      "-DUSE_SHARED_MBEDTLS_LIBRARY=ON",
    )
  },
  createFramework = {
    doLast {
      if (type == CreateFramework.FrameworkType.static) return@doLast
      for (framework in dependency.frameworks) {
        val dir = project.rootDir.resolve("fat-frameworks/shared/$platform/${framework.frameworkName}.framework")
        val links = let {
          val result = execExpectingResult {
            workingDir = dir
            command = arrayOf("otool", "-L", framework.frameworkName)
          }
          result.lines()
            .drop(1)
            .mapNotNull { it.trim().split(" ").firstOrNull()?.trim() }
        }
        for (link in links) {
          val newPath = when {
            link.startsWith("@rpath/libmbedx509.") -> "@rpath/Mbedx509"
            link.startsWith("@rpath/libmbedcrypto.") -> "@rpath/Mbedcrypto"
            link.startsWith("@rpath/libeverest.") -> "@rpath/Everest"
            link.startsWith("@rpath/libp256m.") -> "@rpath/P256m"
            else -> continue
          }
          execExpectingSuccess {
            workingDir = dir
            command = arrayOf(
              "install_name_tool", "-change", link, newPath, framework.frameworkName
            )
          }
        }
      }
    }
  },
  postBuild = {
    doLast {
      execExpectingSuccess {
        val context = buildContext(Dependency.mbedtls, it)
        val libDir = context.prefixDir.resolve("lib")
        workingDir = libDir
        command = arrayOf(
          "clang", "-dynamiclib",
          "-o", "libmbedtls_combined.dylib",
          *libDir.listFiles()
            .filter { it.extension == "a" }
            .map { it.absolutePath }
            .toTypedArray(),
          "-install_name @rpath/libmbedtls_combined.dylib",
          "-current_version", Dependency.mbedtls.versionName.removePrefix("v"),
          "-compatibility_version", "3.0.0",
          "-lc++",
        )
      }
    }
  }
)
