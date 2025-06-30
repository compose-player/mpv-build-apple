import fr.composeplayer.builds.apple.utils.BUILD_VERSION
import fr.composeplayer.builds.apple.utils.execExpectingResult
import fr.composeplayer.builds.apple.utils.exists
import org.gradle.kotlin.dsl.support.zipTo

plugins {
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.kotlin.dsl) apply false
}

afterEvaluate {

  this.version = BUILD_VERSION

  val printVersion by tasks.registering {
    doLast { print(BUILD_VERSION) }
  }

  val createArtifacts by tasks.registering {
    doLast {
      val types = listOf("shared", "static")
      for (type in types) {
        val xcf = rootDir.resolve("xcframeworks/$type").listFiles()!!
        for (dir in xcf) {
          val destination = rootDir.resolve("zips/${dir.name}-$type.zip")
          if (!destination.parentFile.exists) destination.parentFile.mkdirs()
          zipTo(destination, dir)

        }
        val platforms = rootDir.resolve("fat-frameworks").resolve(type).listFiles()!!
        for (platform in platforms) {
          val frameworks = platform.listFiles()!!
          for (framework in frameworks) {
            val destination = rootDir.resolve("zips/${framework.name}-${platform.name}-$type.zip")
            if (!destination.parentFile.exists) destination.parentFile.mkdirs()
            zipTo(destination, framework)

          }
        }
      }


    }
  }

  val clean by tasks.registering {
    group = "build"
    doLast {
      rootDir.resolve("build").deleteRecursively()
      rootDir.resolve("builds").deleteRecursively()
      rootDir.resolve("binaries").deleteRecursively()
      rootDir.resolve("vendor").deleteRecursively()
      rootDir.resolve("cross-files").deleteRecursively()
      rootDir.resolve("fat-frameworks").deleteRecursively()
      rootDir.resolve("xcframeworks").deleteRecursively()
    }
  }

  val checkLinks by tasks.registering {
    val dir = rootDir.resolve("fat-frameworks/shared").listFiles().toList().flatMap { it.listFiles().toList() }
    for (framework in dir) {
      val isFramework = framework.name.endsWith(".framework")
      if (!isFramework) continue
      val links = let {
        val result = execExpectingResult {
          workingDir = framework
          command = arrayOf("otool", "-L", framework.name.removeSuffix(".framework"))
        }
        result.lines()
          .drop(1)
          .mapNotNull { it.trim().split(" ").firstOrNull()?.trim() }
      }
      logger.lifecycle("Framework ${framework.name} links:")
      for (l in links) {
        logger.lifecycle("${framework.name} -> $l")
        if (l.startsWith("@rpath") && l.contains("dylib")) logger.lifecycle(l)
      }
    }
  }

}