import fr.composeplayer.builds.apple.misc.Architecture
import fr.composeplayer.builds.apple.misc.Dependency
import fr.composeplayer.builds.apple.misc.Platform
import fr.composeplayer.builds.apple.misc.name
import fr.composeplayer.builds.apple.tasks.AutoBuildTask
import fr.composeplayer.builds.apple.tasks.CloneTask
import fr.composeplayer.builds.apple.tasks.applyFrom

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
      val file = File(rootProject.rootDir, "vendor/${Dependency.dav1d}")
      if (file.exists()) file.deleteRecursively()
    }
  }

  tasks.register<CloneTask>("clone") {
    applyFrom(Dependency.dav1d)
  }

  val buildAll by tasks.register<Task>("buildAll")

  for (platform in platforms) {
    tasks.register(
      name = "build[${platform.name}][${platform.arch.name}]",
      type = AutoBuildTask::class
    ) {
      buildAll.dependsOn(this)
      this.dependency = Dependency.dav1d
      this.platform = platform
      this.arguments = arrayOf(
        "-Denable_asm=false",
        "-Denable_tests=false",
        "-Denable_tools=false",
        "-Denable_examples=false",
        "-Dxxhash_muxer=disabled",
      )
    }
  }



}


