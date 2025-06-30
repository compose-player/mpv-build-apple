import fr.composeplayer.builds.apple.misc.BuildTarget
import fr.composeplayer.builds.apple.misc.Dependency
import fr.composeplayer.builds.apple.misc.Platform
import fr.composeplayer.builds.apple.misc.buildTag
import fr.composeplayer.builds.apple.tasks.CreateXcFramework.FrameworkType
import fr.composeplayer.builds.apple.tasks.buildContext
import fr.composeplayer.builds.apple.utils.*

plugins {
  kotlin("jvm")
}

group = "fr.composeplayer.builds.mpv"
version = BUILD_VERSION

repositories { mavenCentral() }
kotlin { jvmToolchain(23) }

val targets = DEFAULT_TARGETS.flatMap { (platform, archs) -> archs.map { BuildTarget(platform, it) } }

registerBasicWorkflow(
  targets = DEFAULT_TARGETS,
  dependency = Dependency.moltenvk,
  prebuild = {
    enabled = !rootProject.rootDir.resolve("vendor/${Dependency.moltenvk}/External").exists()
    doLast {
      val sourceDir = File(rootProject.rootDir, "vendor/${Dependency.moltenvk}")
      val deps = targets.map { "--${it.platform.buildTag}" }.toSet().toTypedArray()
      execExpectingSuccess {
        workingDir = sourceDir
        command = arrayOf(
          sourceDir.resolve("fetchDependencies").absolutePath,
          *deps
        )
      }
    }
  },
  build = {
    arguments = emptyArray()
    skip = true
    doLast {
      val sourceDir = File(rootProject.rootDir, "vendor/${Dependency.moltenvk}")
      execExpectingSuccess {
        workingDir = sourceDir
        command = arrayOf("make", "clean")
      }
      execExpectingSuccess {
        val args = targets.map { it.platform.buildTag }.toSet().toTypedArray()
        workingDir = sourceDir
        command = arrayOf("make", *args)
      }
    }
  },
  postBuild = {
    doLast {
      val rawVersion = execExpectingResult {
        val sourceDir = File(rootProject.rootDir, "vendor/${Dependency.moltenvk}")
        workingDir = sourceDir.resolve("External/Vulkan-Headers")
        command = arrayOf("git", "describe", "--tags", "HEAD")
      }
      val vulkanVersion = rawVersion.split("-").first()
      val sourceDir = File(rootProject.rootDir, "vendor/${Dependency.moltenvk}")
      for (target in targets) {
        val context = buildContext(Dependency.moltenvk, target)
        val subs = listOf("static", "dynamic")
        for (sub in subs) {
          val binDir = when (target.platform) {
            Platform.ios -> "ios-arm64"
            Platform.isimulator -> "ios-arm64_x86_64-simulator"
            Platform.macos -> "macos-arm64_x86_64"
          }
          val sourceFile = when (sub) {
            "static" -> sourceDir.resolve("Package/Release/MoltenVK/$sub/MoltenVK.xcframework/$binDir/libMoltenVK.a")
            "dynamic" -> sourceDir.resolve("Package/Release/MoltenVK/$sub/MoltenVK.xcframework/$binDir/MoltenVK.framework/MoltenVK")
            else -> throw GradleException("Unknown bin name: $sub")
          }
          val destination = context.prefixDir.resolve("lib/${sourceFile.name}")
          if (!destination.parentFile.exists) destination.parentFile.mkdirs()
          if (destination.exists()) continue
          sourceFile.copyTo(destination)
        }
        val frameworks = let {
          val list = buildList {
            add("CoreFoundation", "CoreGraphics", "Foundation", "IOSurface", "Metal", "QuartzCore")
            if (target.platform != Platform.macos) add("UIKit")
            if (target.platform == Platform.macos) add("Cocoa")
            add("IOKit")
          }
          list.joinToString(separator = " ", transform = { "-framework $it"})
        }
        val pcContent = """
          prefix=${context.prefixDir.absolutePath}
          includedir=${'$'}{prefix}/include
          libdir=${'$'}{prefix}/lib

          Name: Vulkan-Loader
          Description: Vulkan Loader
          Version: ${vulkanVersion.removePrefix("v")}
          Libs: -L${'$'}{libdir} -lMoltenVK $frameworks
          Cflags: -I${'$'}{includedir}
        """
        val pcFile = context.prefixDir.resolve("lib/pkgconfig/vulkan.pc")
        if (!pcFile.parentFile.exists) pcFile.parentFile.mkdirs()
        pcFile.writeText( pcContent.trimIndent() )

        val vulkanIncludes = sourceDir.resolve("Package/Release/MoltenVK/include").listFiles()!!

        for (dir in vulkanIncludes) {
          val destination = context.prefixDir.resolve("include/${dir.name}")
          dir.copyRecursively(target = destination, overwrite = true)
        }
      }
    }
  },
  createXcframework = {
    doLast {
      val dir = project.rootDir.resolve("vendor/${Dependency.moltenvk}/Package/Release/MoltenVK")
      val static = dir.resolve("static/MoltenVK.xcframework")
      val shared = dir.resolve("dynamic/MoltenVK.xcframework")

      val staticDestination = rootDir.resolve("xcframeworks/${FrameworkType.static}/MoltenVK.xcframework")
        .apply(File::deleteRecursively)
        .apply(File::mkdirs)

      val sharedDestination = rootDir.resolve("xcframeworks/${FrameworkType.shared}/MoltenVK.xcframework")
        .apply(File::deleteRecursively)
        .apply(File::mkdirs)

      static.copyRecursively(staticDestination, true)
      shared.copyRecursively(sharedDestination, true)

    }
  }
)


