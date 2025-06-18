package fr.composeplayer.builds.apple.tasks

import fr.composeplayer.builds.apple.misc.Architecture
import fr.composeplayer.builds.apple.misc.BuildTarget
import fr.composeplayer.builds.apple.misc.Dependency
import fr.composeplayer.builds.apple.misc.FrameworkCreationData
import fr.composeplayer.builds.apple.misc.Platform
import fr.composeplayer.builds.apple.misc.frameworks
import fr.composeplayer.builds.apple.misc.sdk
import fr.composeplayer.builds.apple.utils.BUILD_VERSION
import fr.composeplayer.builds.apple.utils.execExpectingResult
import fr.composeplayer.builds.apple.utils.execExpectingSuccess
import fr.composeplayer.builds.apple.utils.exists
import fr.composeplayer.builds.apple.utils.minVersion
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import java.io.File

abstract class CreateFramework : DefaultTask() {

  enum class FrameworkType { static, shared }

  @Input
  lateinit var dependency: Dependency

  @Input
  lateinit var platform: Platform

  @Input
  lateinit var architectures: List<Architecture>

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
      val installDir = project.rootDir.resolve("fat-frameworks/$type/$platform/${framework.frameworkName}.framework")
      if (installDir.exists) {
        installDir.deleteRecursively()
        logger.lifecycle("Framework [$framework] already exists, deleting old files")
      }
      installDir.mkdirs()
      val command = mutableListOf("lipo", "-create")
      for (arch in architectures) {
        val target = BuildTarget(platform, arch)
        val context = buildContext(dependency, target)
        val include = context.prefixDir.resolve("include")
        val destination = installDir.resolve("Headers").apply(File::mkdir)
        include.copyRecursively(
          target = destination,
          overwrite = true,
        )
        val binaryExtension = if (type == FrameworkType.static) "a" else "dylib"
        val binaryFile = let {
          val names = when {
            framework.frameworkName == "Shaderc_combined" -> setOf(
              "libshaderc_combined.$binaryExtension",
              "libshaderc_shared.$binaryExtension"
            )
            else -> setOf(
              "lib${framework.frameworkName}.$binaryExtension".lowercase(),
              "${framework.frameworkName}.$binaryExtension".lowercase(),
            )
          }
          context.prefixDir
            .resolve("lib")
            .listFiles()
            .first { it.name.lowercase() in names }
        }
        command += binaryFile.absolutePath
      }
      command += listOf("-output", installDir.resolve(framework.frameworkName).absolutePath)

      execExpectingSuccess { this.command = command.toTypedArray() }

      val modulemap = modulemap(
        frameworkName = framework.frameworkName,
        excludeHeaders = framework.excludeHeaders,
      )
      installDir.resolve("Modules/module.modulemap")
        .apply {
          if (!parentFile.exists) parentFile.mkdirs()
          writeText(modulemap)
        }
      val plist = plist(framework.frameworkName, platform)
      installDir.resolve("Info.plist").writeText(plist)
    }
  }

}







fun modulemap(
  frameworkName: String,
  excludeHeaders: List<String>,
): String {
  return buildString {
    appendLine("""framework module ${frameworkName} [system] {""")
    appendLine("""  umbrella "."""")
    for (exclude in excludeHeaders) appendLine("""  exclude header $exclude.h""")
    appendLine("""  export *""")
    appendLine("}")
  }
}

private fun plist(
  frameworkName: String,
  platform: Platform,
): String {
  val identifier = "fr.composeplayer.$frameworkName"
  val content = """
    <?xml version="1.0" encoding="UTF-8"?>
    <!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
    <plist version="1.0">
    <dict>
    <key>CFBundleDevelopmentRegion</key>
    <string>en</string>
    <key>CFBundleExecutable</key>
    <string>$frameworkName</string>
    <key>CFBundleIdentifier</key>
    <string>$identifier</string>
    <key>CFBundleInfoDictionaryVersion</key>
    <string>6.0</string>
    <key>CFBundleName</key>
    <string>$frameworkName</string>
    <key>CFBundlePackageType</key>
    <string>FMWK</string>
    <key>CFBundleShortVersionString</key>
    <string>${BUILD_VERSION}</string>
    <key>CFBundleVersion</key>
    <string>$BUILD_VERSION</string>
    <key>CFBundleSignature</key>
    <string>????</string>
    <key>MinimumOSVersion</key>
    <string>${platform.minVersion}</string>
    <key>CFBundleSupportedPlatforms</key>
    <array>
    <string>${platform.sdk}</string>
    </array>
    <key>NSPrincipalClass</key>
    <string></string>
    </dict>
    </plist>
  """
  return content.trimIndent()
}