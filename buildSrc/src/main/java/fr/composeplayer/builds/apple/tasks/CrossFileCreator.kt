package fr.composeplayer.builds.apple.tasks

import fr.composeplayer.builds.apple.misc.cpuFamily
import fr.composeplayer.builds.apple.misc.mesonSubSystem
import fr.composeplayer.builds.apple.misc.name
import fr.composeplayer.builds.apple.misc.targetCpu
import fr.composeplayer.builds.apple.utils.locationOf
import java.io.File

class CrossFileCreator(
  private val context: BuildContext,
) {

  val ldFlags: String
    get() = ""

  val cFlags: String
    get() = ""

  fun create(): File {
    val file = File(context.project.rootDir, "cross-files/${context.platform.name}-${context.platform.arch.name}.pc")
    if (file.exists()) return file
    file.parentFile.mkdirs()
    file.createNewFile()
    val content = """
      [binaries]
      c = '/usr/bin/clang'
      cpp = '/usr/bin/clang++'
      objc = '/usr/bin/clang'
      objcpp = '/usr/bin/clang++'
      ar = '${context.platform.locationOf("ar")}'
      strip = '${context.platform.locationOf("strip")}'
      pkg-config = 'pkg-config'
      
      [properties]
      has_function_printf = true
      has_function_hfkerhisadf = false
      
      [host_machine]
      system = 'darwin'
      subsystem = '${context.platform.mesonSubSystem}'
      kernel = 'xnu'
      cpu_family = '${context.platform.arch.cpuFamily}'
      cpu = '${context.platform.arch.targetCpu}'
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
