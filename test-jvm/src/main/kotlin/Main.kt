package fr.composeplayer.builds.mpv

import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.MemorySegment
import java.lang.foreign.SymbolLookup
import java.lang.foreign.ValueLayout
import java.lang.invoke.MethodHandle
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths

class Main {

  companion object {

    @JvmStatic
    fun getMpvVersion(): Long {
      val uri = this::class.java.getResource("/libs/")!!.toURI()
      val resources = try {
        Paths.get(uri)
      } catch (_: Throwable) {
        val env = mutableMapOf<String, String>()
        FileSystems.newFileSystem(uri, env).getPath("/libs/")
      }
      val nativeLibs = Files.list(resources).toList().map { it.toFile() }
      val tempDir = Files.createTempDirectory("native-libs").toFile()
      for (file in nativeLibs) {
        val target = tempDir.resolve(file.name)
        file.copyTo(target)
      }
      val mpv = tempDir.resolve("Mpv")
      System.load(mpv.absolutePath)

      val linker = Linker.nativeLinker()
      val lookup = SymbolLookup.loaderLookup()

      val mpv_client_api_version: MethodHandle = linker.downcallHandle(
        lookup.find("mpv_client_api_version").orElseThrow(),
        FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS)
      )
      return mpv_client_api_version.invokeExact(MemorySegment.NULL) as Long
    }

    @JvmStatic
    fun main(args: Array<String>) {
      val version = getMpvVersion()
      print("Mpv loaded, version: $version")
    }

  }

}