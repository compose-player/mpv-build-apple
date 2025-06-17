import fr.composeplayer.builds.apple.misc.Dependency
import fr.composeplayer.builds.apple.misc.host
import fr.composeplayer.builds.apple.misc.versionName
import fr.composeplayer.builds.apple.tasks.buildContext
import fr.composeplayer.builds.apple.utils.DEFAULT_TARGETS
import fr.composeplayer.builds.apple.utils.registerBasicWorkflow

plugins {
  kotlin("jvm")
}

group = "fr.composeplayer.builds.mpv"
version = libs.versions.library

repositories { mavenCentral() }
kotlin { jvmToolchain(23) }

registerBasicWorkflow(
  targets = DEFAULT_TARGETS,
  dependency = Dependency.spirvcross,
  build = {
    this.arguments = arrayOf(
      "-DSPIRV_CROSS_SHARED=ON",
      "-DSPIRV_CROSS_STATIC=ON",
      "-DSPIRV_CROSS_CLI=OFF",
      "-DSPIRV_CROSS_ENABLE_TESTS=OFF",
      "-DSPIRV_CROSS_FORCE_PIC=ON",
      "-Ddemos=false-DSPIRV_CROSS_ENABLE_CPP=OFF"
    )
  },
  postBuild = { target ->
    doLast {
      val context = buildContext(Dependency.spirvcross, target)
      val vulkanVersion = Dependency.spirvcross.versionName.removePrefix("vulkan-sdk-")
      val pcFile = context.prefixDir.resolve("lib/pkgconfig/spirv-cross-c-shared.pc").apply(File::delete)
      val pcContent = """
          prefix=${context.prefixDir.absolutePath}
          exec_prefix=${'$'}{prefix}
          includedir=${'$'}{prefix}/include/spirv_cross
          libdir=${'$'}{prefix}/lib
          
          Name: spirv-cross-c-shared
          Description: C API for SPIRV-Cross
          Version: $vulkanVersion
          Libs: -L${'$'}{libdir} -lspirv-cross-c -lspirv-cross-glsl -lspirv-cross-hlsl -lspirv-cross-reflect -lspirv-cross-msl -lspirv-cross-util -lspirv-cross-core -lstdc++
          Cflags: -I${'$'}{includedir}
        """
      pcFile.writeText( pcContent.trimIndent() )
    }
  },
)
