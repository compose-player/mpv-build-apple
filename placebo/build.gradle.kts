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
      val file = File(rootProject.rootDir, "vendor/${Dependency.placebo.name}")
      if (file.exists()) file.deleteRecursively()
    }
  }

  tasks.register<CloneTask>("clone") {
    applyFrom(Dependency.placebo)
  }

  val buildAll by tasks.register<Task>("buildAll")

  for (platform in platforms) {
    tasks.register(
      type = AutoBuildTask::class,
      name = "build[${platform.name}][${platform.arch.name}]",
    ) {
      buildAll.dependsOn(this)
      this.dependency = Dependency.placebo
      this.platform = platform
      this.arguments = arrayOf(
        "-Dopengl=enabled",
        "-Dvulkan=enabled",
        "-Dshaderc=enabled",
        "-Dlcms=enabled",
        "-Dxxhash=disabled",
        "-Dunwind=disabled",
        "-Dglslang=disabled",
        "-Dd3d11=disabled",
        "-Ddemos=false",
        "-Dtests=false",
        "-Ddovi=enabled", "-Dlibdovi=enabled"
      )
    }

  }



}


