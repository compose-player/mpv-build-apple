package fr.composeplayer.builds.apple.tasks

import fr.composeplayer.builds.apple.misc.cpuFamily
import fr.composeplayer.builds.apple.misc.mesonSubSystem
import fr.composeplayer.builds.apple.misc.targetCpu
import fr.composeplayer.builds.apple.utils.locationOf
import java.io.File

class CrossFileCreator(
  private val context: BuildContext,
) {

  private val cFlags: String
    get() = context.cFlags.joinToString(
      separator = ", ",
      transform = { "'$it'" },
    )

  private val ldFlags: String
    get() = context.ldFlags.joinToString(
      separator = ", ",
      transform = { "'$it'" },
    )

  fun create(): File {
    val file = File(context.project.rootDir, "cross-files/${context.buildTarget.platform.name}-${context.buildTarget.arch.name}.pc")
    file.parentFile.mkdirs()
    val content = """
      [binaries]
      c = '/usr/bin/clang'
      cpp = '/usr/bin/clang++'
      objc = '/usr/bin/clang'
      objcpp = '/usr/bin/clang++'
      ar = '${context.buildTarget.platform.locationOf("ar")}'
      strip = '${context.buildTarget.platform.locationOf("strip")}'
      pkg-config = 'pkg-config'
      
      [properties]
      has_function_printf = true
      has_function_hfkerhisadf = false
      
      [host_machine]
      system = 'darwin'
      subsystem = '${context.buildTarget.platform.mesonSubSystem}'
      kernel = 'xnu'
      cpu_family = '${context.buildTarget.arch.cpuFamily}'
      cpu = '${context.buildTarget.arch.targetCpu}'
      endian = 'little'
      
      [built-in options]
      default_library = 'both'
      buildtype = 'release'
      prefix = '${context.prefixDir.absolutePath}'
      c_args = [$cFlags]
      cpp_args = [$cFlags]
      objc_args = [$cFlags]
      objcpp_args = [$cFlags]
      c_link_args = [$ldFlags]
      cpp_link_args = [$ldFlags]
      objc_link_args = [$ldFlags]
      objcpp_link_args = [$ldFlags]
    """
    val trimmed = content.trimIndent()
    return file.apply { writeText(trimmed) }
  }

}
