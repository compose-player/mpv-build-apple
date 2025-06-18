import fr.composeplayer.builds.apple.utils.execExpectingResult
import fr.composeplayer.builds.apple.utils.execExpectingSuccess
import fr.composeplayer.builds.apple.utils.exists

plugins {
  kotlin("jvm") version "2.1.20"
  application
}

group = "fr.composeplayer.builds.mpv"
version = "unspecified"

repositories {
  mavenCentral()
}

dependencies {
  testImplementation(kotlin("test"))
}

tasks.test {
  useJUnitPlatform()
}
kotlin {
  jvmToolchain(23)
}

application {
  mainClass = "fr.composeplayer.builds.mpv.Main"
}


val copyLibs by tasks.registering {
  group = "application"
  val dirs = rootProject.rootDir.resolve("fat-frameworks/shared/macos").listFiles()!!
  for (framework in dirs) {
    val name = framework.name.removeSuffix(".framework")
    val file = framework.resolve(name)
    val destination = projectDir.resolve("src/main/resources/libs/$name")
    if (!destination.parentFile.exists) {
      destination.parentFile.mkdirs()
    }
    file.copyTo(destination, true)

    val rpathCheck = execExpectingResult {
      workingDir = framework
      command = arrayOf("otool", "-l", name)
    }

    val hasPath = rpathCheck.contains("path @loader_path")
    if (hasPath) {
      logger.lifecycle("Skipping install_name_tool for framework $name")
      continue
    }
    execExpectingSuccess {
      workingDir = framework
      command = arrayOf("install_name_tool", "-add_rpath", "@loader_path", name)
    }
  }
}

val compileKotlin by tasks.getting {
  dependsOn(copyLibs)
}