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
      val file = File(rootProject.rootDir, "vendor/${Dependency.harfbuzz.name}")
      if (file.exists()) file.deleteRecursively()
    }
  }

  tasks.register<CloneTask>("clone") {
    applyFrom(Dependency.harfbuzz)
  }

  val buildAll by tasks.register<Task>("buildAll")

  for (platform in platforms) {
    tasks.register(
      name = "build[${platform.name}][${platform.arch.name}]",
      type = AutoBuildTask::class,
    ) {
      buildAll.dependsOn(this)
      this.platform = platform
      this.dependency = Dependency.harfbuzz
      this.arguments = arrayOf(
        "-Dtests=disabled",
        "-Ddocs=disabled",
        "-Dglib=disabled",
        "-Dcairo=disabled",
      )
    }
  }



}


