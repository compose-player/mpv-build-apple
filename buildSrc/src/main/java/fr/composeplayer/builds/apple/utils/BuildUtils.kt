package fr.composeplayer.builds.apple.utils

import fr.composeplayer.builds.apple.misc.Architecture
import fr.composeplayer.builds.apple.misc.BuildTarget
import fr.composeplayer.builds.apple.misc.Dependency
import fr.composeplayer.builds.apple.misc.Platform
import fr.composeplayer.builds.apple.misc.frameworks
import fr.composeplayer.builds.apple.misc.sdk
import fr.composeplayer.builds.apple.misc.targetCpu
import fr.composeplayer.builds.apple.tasks.AutoBuildTask
import fr.composeplayer.builds.apple.tasks.CloneTask
import fr.composeplayer.builds.apple.tasks.CreateFramework
import fr.composeplayer.builds.apple.tasks.CreateXcFramework
import fr.composeplayer.builds.apple.tasks.applyFrom
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.kotlin.dsl.getValue
import org.gradle.process.ExecOperations
import org.gradle.process.ExecSpec
import java.io.File
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator
import kotlin.concurrent.thread

val BUILD_VERSION = "1.0.0"

class ExecScope(
  private val execSpec: ExecSpec,
) : ExecSpec by execSpec{

  var command: Array<String>
    set(value) = setCommandLine(*value)
    get() = commandLine.orEmpty().toTypedArray()
}

fun ExecOperations.execExpectingSuccess(
  block: ExecScope.() -> Unit,
) {
  val result = exec {
    standardOutput = System.out
    errorOutput = System.err
    val scope = ExecScope(this)
    environment["PATH"] = System.getenv("PATH")
    block.invoke(scope)
  }
  if (result.exitValue != 0) throw GradleException("Exec operation failed with result code [$result]")
}

interface CommandScope {
  interface Environment {
    operator fun get(key: String): String?
    operator fun set(key: String, value: String?)
  }
  val env: Environment
  var command: Array<String>
  var workingDir: File?
}


private class ProcessBuilderScope(
  private val builder: ProcessBuilder,
) : CommandScope {

  override var workingDir: File?
    get() = builder.directory()
    set(value) { builder.directory(value) }

  override var command: Array<String>
    get() = builder.command().toTypedArray()
    set(value) {
      builder.command(
        "zsh", "-l", "-c",
        value.joinToString(" "),
      )
    }

  override val env: CommandScope.Environment = object : CommandScope.Environment {
    override fun get(key: String): String? = builder.environment()[key]
    override fun set(key: String, value: String?) {
      when {
        value == null -> builder.environment().remove(key)
        else -> builder.environment().set(key, value)
      }
    }
  }
}

fun Task.execExpectingSuccess(
  block: CommandScope.() -> Unit,
) {
  val builder = ProcessBuilder()
  ProcessBuilderScope(builder).apply(block)
  logger.lifecycle("Executing command:")
  logger.lifecycle(builder.command().joinToString(" "))
  val process = builder.start()
  val thread = thread { process.inputStream.copyTo(System.out) }
  val thread2 = thread { process.errorStream.copyTo(System.err) }
  val resultCode = process.waitFor()
  thread.join()
  thread2.join()
  if (resultCode != 0) throw GradleException("Command failed with result code [$resultCode]")
}

fun Task.execExpectingResult(
  block: CommandScope.() -> Unit,
): String {
  val builder = ProcessBuilder()
  ProcessBuilderScope(builder).apply(block)
  logger.lifecycle("Executing command:")
  logger.lifecycle(builder.command().joinToString(" "))
  val process = builder.start().apply {
    errorStream.copyTo(System.err)
  }
  val result = process.inputStream.bufferedReader().readText()
  val resultCode = process.waitFor()
  if (resultCode != 0) {
    throw GradleException("Command failed with result code [$resultCode]")
  }
  return result

}

val parallelism: Int get() = 4

fun Platform.locationOf(toolName: String): String {
  return when (toolName) {
    "ar", "strip", "--show-sdk-path" -> {
      val cmd = arrayOf("xcrun", "--sdk", this.sdk.lowercase(), "--find", toolName)
      val process = ProcessBuilder(*cmd).start()
      val output = process.inputStream.bufferedReader().readText().trim()
      val resultCode = process.waitFor()
      if (resultCode != 0) throw GradleException("Cannot find tool [$toolName]")
      output
    }
    else -> throw GradleException("Unknown platform tool '$toolName'")
  }
}

fun <T> MutableList<T>.add(vararg elements: T) = addAll(elements.toList())

val Platform.isysroot: String
  get() = locationOf("--show-sdk-path")

val Platform.minVersion: String
  get() = when (this) {
    Platform.ios, Platform.isimulator -> "13.0"
    Platform.macos -> "11.0"
  }
val Platform.osVersionMin: String
  get() = when (this) {
    Platform.ios -> "-m$name-version-min=$minVersion"
    Platform.isimulator -> "-mios-simulator-version-min=$minVersion"
    Platform.macos -> "-mmacosx-version-min=$minVersion"
  }

val BuildTarget.deploymentTarget: String
  get() = when (platform) {
    Platform.ios, Platform.macos -> "${arch.targetCpu}-apple-${platform.name}${platform.minVersion}"
    Platform.isimulator -> "${arch.targetCpu}-apple-${Platform.ios.name}${platform.minVersion}-simulator"
  }

val BuildTarget.cFlags: List<String>
  get() = buildList {
    add(
      "-arch", arch.name,
      "-isysroot", platform.isysroot,
      "-target", deploymentTarget, platform.osVersionMin,
    )
  }

val BuildTarget.ldFlags: List<String>
  get() = buildList {
    add(
      "-lc++",
      "-arch", arch.name,
      "-isysroot", platform.isysroot,
      "-target", deploymentTarget, platform.osVersionMin
    )
  }

val BuildTarget.frameworkDirectory: String
  get() = when (this.platform) {
    Platform.ios -> "ios-arm64"
    Platform.isimulator -> "ios-arm64_x86_64-simulator"
    Platform.macos -> "macos-arm64_x86_64"
  }

val File.exists: Boolean get() = exists()


val DEFAULT_TARGETS  = mapOf(
  Platform.ios to listOf(Architecture.arm64),
  Platform.isimulator to listOf(Architecture.arm64, Architecture.x86_64),
  Platform.macos to listOf(Architecture.arm64, Architecture.x86_64),
)




fun Project.registerBasicWorkflow(
  group: String = "build workflow",
  dependency: Dependency,
  targets: Map<Platform, List<Architecture>>,
  prebuild: Task.(target: BuildTarget) -> Unit = { enabled = false },
  build: AutoBuildTask.() -> Unit,
  postBuild: Task.(target: BuildTarget) -> Unit = { enabled = false },
  createFramework: CreateFramework.() -> Unit = {
    this.enabled = dependency.frameworks.isNotEmpty()
  },
  createXcframework: CreateXcFramework.() -> Unit = {
    this.enabled = dependency.frameworks.isNotEmpty()
  },
) {

  val buildTargets = targets.flatMap { (platform, archs) -> archs.map { BuildTarget(platform, it) } }

  val buildAllTask by tasks.register("build[all]") {
    this.group = group
  }

  val createDynamicFrameworks by tasks.register("createFramework[all][dynamic]") {
    this.group = group
  }

  val createStaticFrameworks by tasks.register("createFramework[all][static]") {
    this.group = group
  }

  val createAllFrameworksTask by tasks.register("createFramework[all][both]") {
    this.group = group
  }

  createAllFrameworksTask.dependsOn(createDynamicFrameworks, createStaticFrameworks)


  val createXcframeworkTask by tasks.register("createXcframework[both]") {
    this.group = group
  }

  val cloneTask by tasks.register(
    /* name = */  "clone",
    /* type = */  CloneTask::class.java,
    /* configurationAction = */  {
      this.group = group
      this.applyFrom(dependency)
    },
  )

  for (target in buildTargets) {

    val prebuildTask by tasks.register(
      /* name = */  "preBuild[${target.platform}][${target.arch}]",
      /* type = */  Task::class.java,
      /* configurationAction = */  {
        this.group = group
        prebuild.invoke(this, target)
      },
    )
    val buildTask by tasks.register(
      /* name = */  "build[${target.platform}][${target.arch}]",
      /* type = */  AutoBuildTask::class.java,
      /* configurationAction = */  {
        this.group = group
        this.dependency.set(dependency)
        this.buildTarget.set(target)
        build.invoke(this)
      },
    )
    val postBuildTask by tasks.register(
      /* name = */  "postBuild[${target.platform}][${target.arch}]",
      /* type = */  Task::class.java,
      /* configurationAction = */  {
        this.group = group
        postBuild.invoke(this, target)
      },
    )
    prebuildTask.dependsOn(cloneTask)
    buildTask.dependsOn(prebuildTask)
    buildTask.finalizedBy(postBuildTask)
    buildAllTask.dependsOn(buildTask)

  }

  for ( (platform, architectures) in targets) {
    val static by tasks.register(
      /* name = */ "createFramework[static][$platform]",
      /* type = */ CreateFramework::class.java,
      /* configurationAction = */ {
        this.group = group
        this.dependency = dependency
        this.platform = platform
        this.architectures = architectures
        this.type = CreateFramework.FrameworkType.static
        createFramework.invoke(this)
      },
    )
    val shared by tasks.register(
      /* name = */ "createFramework[dynamic][$platform]",
      /* type = */ CreateFramework::class.java,
      /* configurationAction = */ {
        this.group = group
        this.dependency = dependency
        this.platform = platform
        this.architectures = architectures
        this.type = CreateFramework.FrameworkType.shared
        createFramework.invoke(this)
      },
    )
    createDynamicFrameworks.dependsOn(shared)
    createStaticFrameworks.dependsOn(static)
  }

  val createStaticXcframework by tasks.register(
    /* name = */ "createXcframework[static]",
    /* type = */ CreateXcFramework::class.java,
    /* configurationAction = */ {
      this.group = group
      this.dependency = dependency
      this.type = CreateXcFramework.FrameworkType.static
      createXcframework.invoke(this)
    }
  )

  val createDynamicXcframework by tasks.register(
    /* name = */ "createXcframework[dynamic]",
    /* type = */ CreateXcFramework::class.java,
    /* configurationAction = */ {
      this.group = group
      this.dependency = dependency
      this.type = CreateXcFramework.FrameworkType.shared
      createXcframework.invoke(this)

    }
  )

  createXcframeworkTask.dependsOn(createStaticXcframework, createDynamicXcframework)



}
