package fr.composeplayer.builds.apple.tasks

import fr.composeplayer.builds.apple.misc.Dependency
import fr.composeplayer.builds.apple.misc.frameworks
import fr.composeplayer.builds.apple.utils.add
import fr.composeplayer.builds.apple.utils.execExpectingSuccess
import fr.composeplayer.builds.apple.utils.exists
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

abstract class CreateXcFramework : DefaultTask() {

  enum class FrameworkType { static, shared }

  @Input
  lateinit var dependency: Dependency

  @Input
  lateinit var type: FrameworkType

  @get:Optional
  @get:Input
  abstract val skip: Property<Boolean>

  @TaskAction
  fun execute() {
    if (skip.isPresent && skip.get()) return
    for (framework in dependency.frameworks) {
      if (framework.dynamicOnly && type == FrameworkType.static) continue
      val arguments = mutableListOf("xcodebuild", "-create-xcframework")
      val platforms = project.rootDir.resolve("fat-frameworks/$type").listFiles()
      for (platform in platforms) {
        val frameworkFile = platform.resolve("${framework.frameworkName}.framework")
        arguments.add("-framework", frameworkFile.absolutePath)
      }
      val outputDir = project.rootDir.resolve("xcframeworks/$type/${framework.frameworkName}.xcframework")
      if (outputDir.exists) outputDir.deleteRecursively()
      arguments.add("-output", outputDir.absolutePath)
      execExpectingSuccess {
        command = arguments.toTypedArray()
      }
    }
  }

}
