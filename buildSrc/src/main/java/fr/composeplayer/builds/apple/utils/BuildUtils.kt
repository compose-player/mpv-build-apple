package fr.composeplayer.builds.apple.utils

import fr.composeplayer.builds.apple.misc.Platform
import fr.composeplayer.builds.apple.misc.sdk
import org.gradle.api.GradleException
import org.gradle.internal.cc.base.logger
import org.gradle.process.ExecOperations
import org.gradle.process.ExecSpec
import java.io.File
import java.io.InputStream
import java.io.OutputStream

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

class CommandScope(val process: ProcessBuilder) {

  interface Environment {
    operator fun get(key: String): String?
    operator fun set(key: String, value: String?)
  }

  val env: Environment = object : Environment {
    override fun set(key: String, value: String?) = process.environment().set(key, value)
    override fun get(key: String): String? = process.environment()[key]
  }

  var command: Array<String> = arrayOf()
    set(value) { process.command(*value) }

  var workingDir: File? = process.directory()
    set(value) { process.directory(value) }

}

fun execExpectingSuccess(
  block: CommandScope.() -> Unit,
) {
  val builder = ProcessBuilder()
  val scope = CommandScope(builder)
  block.invoke(scope)
  val result = builder.start()
    .apply {
      inputStream.copyTo(System.out)
      errorStream.copyTo(System.err)
    }
    .waitFor()
  if (result != 0) {
    val cmd = builder.command().orEmpty().joinToString(separator = " ")
    throw GradleException("Cmd failed with code $result: [$cmd]")
  }
}

fun execExpectingResult(
  block: CommandScope.() -> Unit,
): String {
  val builder = ProcessBuilder()
  val scope = CommandScope(builder)
  block.invoke(scope)
  val process = builder.start()
  val result = process.inputStream.bufferedReader().readText()
  val resultCode = process.waitFor()
  if (resultCode != 0) {
    val cmd = builder.command().orEmpty().joinToString(separator = " ")
    throw GradleException("Cmd failed with code $result: [$cmd]")
  }
  return result
}

val parallelism: Int
  get() = Runtime.getRuntime().availableProcessors()

fun Platform.locationOf(toolName: String): String {
  return when (toolName) {
    "ar", "strip" -> {
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