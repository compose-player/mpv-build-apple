plugins {
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.kotlin.dsl) apply false
}

afterEvaluate {

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

}