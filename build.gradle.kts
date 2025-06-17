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


  for (project in subprojects) {

    project.afterEvaluate {
      val clone by project.tasks.named("clone")
      val buildAll by project.tasks.named("build[all]")
      val frameworks by project.tasks.named("createFramework[all][both]")
      val xcframeworks by project.tasks.named("createXcframework[both]")

      tasks.register(
        name = "assemble[${project.name}]",
        type = Task::class,
        configurationAction = {
          group = "mpv-build"
          buildAll.mustRunAfter(clone)
          frameworks.mustRunAfter(buildAll)
          xcframeworks.mustRunAfter(frameworks)
          dependsOn(clone, buildAll, frameworks, xcframeworks)
        }
      )
    }
  }

}