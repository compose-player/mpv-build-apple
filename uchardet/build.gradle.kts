import fr.composeplayer.builds.apple.tasks.CloneTask
import fr.composeplayer.builds.apple.misc.Dependency
import fr.composeplayer.builds.apple.misc.Platform
import fr.composeplayer.builds.apple.misc.Architecture
import fr.composeplayer.builds.apple.misc.cmakeSystemName
import fr.composeplayer.builds.apple.misc.name
import fr.composeplayer.builds.apple.misc.sdk
import fr.composeplayer.builds.apple.tasks.applyFrom
import fr.composeplayer.builds.apple.tasks.buildContext
import fr.composeplayer.builds.apple.utils.execExpectingSuccess
import fr.composeplayer.builds.apple.utils.parallelism

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
      val file = File(rootProject.rootDir, "vendor/${Dependency.uchardet.name}")
      if (file.exists()) file.deleteRecursively()
    }
  }

  tasks.register<CloneTask>("clone") {
    applyFrom(Dependency.uchardet)
  }

  for (platform in platforms) {
    tasks.register("build[${platform.name}][${platform.arch.name}]") {
      val context = buildContext(Dependency.uchardet, platform)
      doLast {
        if ( !context.sourceDir.exists() ) throw GradleException("Source directory does not exists")
        if ( !context.buildDir.exists() ) context.buildDir.mkdirs()
        execExpectingSuccess {
          workingDir = context.buildDir
          command = arrayOf(
            "cmake", context.sourceDir.absolutePath,
            "-DCMAKE_POLICY_VERSION_MINIMUM=3.5",
            "-DCMAKE_VERBOSE_MAKEFILE=0",
            "-DCMAKE_BUILD_TYPE=Release",
            "-DCMAKE_OSX_SYSROOT=${platform.sdk.lowercase()}",
            "-DCMAKE_OSX_ARCHITECTURES=${platform.arch.name}",
            "-DCMAKE_SYSTEM_NAME=${platform.cmakeSystemName}",
            "-DCMAKE_SYSTEM_PROCESSOR=${platform.arch.name}",
            "-DCMAKE_INSTALL_PREFIX=${context.prefixDir.absolutePath}",
            "-DBUILD_SHARED_LIBS=ON",
          )
        }
        execExpectingSuccess {
          workingDir = context.buildDir
          command = arrayOf("make", "-j$parallelism")
        }
        execExpectingSuccess {
          workingDir = context.buildDir
          command = arrayOf("make", "install")
        }
      }
    }
  }



}


