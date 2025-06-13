@file:OptIn(ExperimentalStdlibApi::class)
@file:Suppress("EnumEntryName")

import fr.composeplayer.builds.apple.misc.Architecture
import fr.composeplayer.builds.apple.misc.Dependency
import fr.composeplayer.builds.apple.misc.Platform
import fr.composeplayer.builds.apple.misc.name
import fr.composeplayer.builds.apple.misc.versionName
import fr.composeplayer.builds.apple.tasks.AutoBuildTask
import fr.composeplayer.builds.apple.tasks.CloneTask
import fr.composeplayer.builds.apple.tasks.applyFrom
import fr.composeplayer.builds.apple.tasks.buildContext
import fr.composeplayer.builds.apple.utils.execExpectingResult
import fr.composeplayer.builds.apple.utils.execExpectingSuccess
import java.nio.file.Files

plugins {
  kotlin("jvm")
}

group = "fr.composeplayer.builds.mpv"
version = libs.versions.library

repositories { mavenCentral() }
kotlin { jvmToolchain(23) }

afterEvaluate {


  val platforms = listOf(
    Platform.MacOS(Architecture.arm64),
    Platform.MacOS(Architecture.x86_64),
    Platform.IOS(Architecture.arm64),
  )

  tasks.getByName("clean") {
    doLast {
      val file = File(rootProject.rootDir, "vendor/${Dependency.moltenvk}")
      if (file.exists()) file.deleteRecursively()
    }
  }

  val buildAllShaderc by tasks.register("buildAllShaderc", Task::class)
  val buildAllSpirvCross by tasks.register("buildAllSpirvCross", Task::class)

  val dependencies = listOf(
    Dependency.moltenvk,
    Dependency.spirvcross,
    Dependency.lcms,
    Dependency.shaderc,
  )

  val cloneDependencies by tasks.register("cloneDependencies")

  for (dep in dependencies) {
    val task = tasks.register(
      name = "clone[${dep.name}]",
      type = CloneTask::class,
    ) {
      applyFrom(dep)
    }
    cloneDependencies.dependsOn(task)
  }


  for (platform in platforms) {

    val shaderc = tasks.register(
      name = "buildShaderC[${platform.name}][${platform.arch.name}]",
      type = AutoBuildTask::class,
      configurationAction = {
        doFirst {
          val context = buildContext(Dependency.shaderc, platform)
          execExpectingSuccess {
            workingDir = context.sourceDir.resolve("utils")
            command = arrayOf("python3", "git-sync-deps")
          }
          val reduce = context.sourceDir.resolve("third_party/spirv-tools/tools/reduce/reduce.cpp")
          val fuzz = context.sourceDir.resolve("third_party/spirv-tools/tools/fuzz/fuzz.cpp")
          Files.write(
            /* path = */ reduce.toPath(),
            /* lines = */ reduce.readLines().toMutableList().also { lines ->
              lines[36] = """FILE* fp = popen(nullptr, "r");"""
              lines[41] = "return fp == NULL;"
            }
          )
          Files.write(
            /* path = */ fuzz.toPath(),
            /* lines = */ fuzz.readLines().toMutableList().also { lines ->
              lines[47] = """FILE* fp = popen(nullptr, "r");"""
              lines[52] = "return fp == NULL;"
            }
          )

        }
        this.platform = platform
        this.dependency = Dependency.shaderc
        this.arguments = arrayOf(
          "-DSHADERC_SKIP_TESTS=ON",
          "-DSHADERC_SKIP_EXAMPLES=ON",
          "-DSHADERC_SKIP_COPYRIGHT_CHECK=ON",
          "-DENABLE_EXCEPTIONS=ON",
          "-DENABLE_GLSLANG_BINARIES=OFF",
          "-DSPIRV_SKIP_EXECUTABLES=ON",
          "-DSPIRV_TOOLS_BUILD_STATIC=ON",
          "-DBUILD_SHARED_LIBS=ON",
        )
      },
    )

    val spirvcross = tasks.register(
      name = "buildSpirvCross[${platform.name}][${platform.arch.name}]",
      type = AutoBuildTask::class,
      configurationAction = {
        this.dependency = Dependency.spirvcross
        this.platform = platform
        arguments = arrayOf(
          "-DSPIRV_CROSS_SHARED=ON",
          "-DSPIRV_CROSS_STATIC=ON",
          "-DSPIRV_CROSS_CLI=OFF",
          "-DSPIRV_CROSS_ENABLE_TESTS=OFF",
          "-DSPIRV_CROSS_FORCE_PIC=ON",
          "-Ddemos=false-DSPIRV_CROSS_ENABLE_CPP=OFF"
        )
        doLast {
          println("dolast")
          val context = buildContext(Dependency.spirvcross, platform)
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
      }
    )

    buildAllShaderc.dependsOn(shaderc)
    buildAllSpirvCross.dependsOn(spirvcross)

  }


  val fetchMoltenVkDependencies by tasks.registering {
    enabled = !rootProject.rootDir.resolve("vendor/${Dependency.moltenvk}/External").exists()
    doLast {
      val sourceDir = File(rootProject.rootDir, "vendor/${Dependency.moltenvk}")
      val deps = platforms.map { "--${it.name}" }.toTypedArray()
      execExpectingSuccess {
        workingDir = sourceDir
        command = arrayOf(
          sourceDir.resolve("fetchDependencies").absolutePath,
          *deps
        )
      }
    }
  }

  val buildMoltenVk by tasks.registering {
    doLast {
      val sourceDir = File(rootProject.rootDir, "vendor/${Dependency.moltenvk}")
      val vulkanVersion = execExpectingResult {
        val sourceDir = File(rootProject.rootDir, "vendor/${Dependency.moltenvk}")
        workingDir = sourceDir.resolve("External/Vulkan-Headers")
        command = arrayOf("git", "describe", "--tags", "HEAD")
      }
      println("vulkanVersion = ${vulkanVersion}")
      execExpectingSuccess {
        workingDir = sourceDir
        command = arrayOf("make", "clean")
      }
      execExpectingSuccess {
        val args = platforms.map { it.name }.toTypedArray()
        workingDir = sourceDir
        command = arrayOf("make", *args)
      }
    }
  }




}


