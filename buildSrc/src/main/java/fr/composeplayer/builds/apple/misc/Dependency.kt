package fr.composeplayer.builds.apple.misc

import fr.composeplayer.builds.apple.utils.add

@Suppress("EnumEntryName")
enum class Dependency {
  ffmpeg,
  ass,
  dav1d,
  placebo,
  freetype,
  harfbuzz,
  fribidi,
  mbedtls,
  shaderc,
  moltenvk,
  uchardet,
  mpv,
  unibreak,
  dovi,
  lcms,
  spirvcross,
}

val Dependency.flagsDependencelibrarys: List<String>
  get() = buildList {
    when (this@flagsDependencelibrarys) {
      Dependency.ffmpeg -> add("mbedtls")
      Dependency.ass -> Unit
      Dependency.dav1d -> Unit
      Dependency.placebo -> add("libdovi")
      Dependency.freetype -> Unit
      Dependency.harfbuzz -> Unit
      Dependency.fribidi -> Unit
      Dependency.mbedtls -> Unit
      Dependency.shaderc -> Unit
      Dependency.moltenvk -> Unit
      Dependency.uchardet -> Unit
      Dependency.mpv -> Unit
      Dependency.unibreak -> Unit
      Dependency.dovi -> Unit
      Dependency.lcms -> Unit
      Dependency.spirvcross -> Unit
    }
  }

val Dependency.cloneArgs: Array<String>
  get() = when (this) {
    Dependency.ffmpeg -> arrayOf()
    Dependency.ass -> arrayOf()
    Dependency.dav1d -> arrayOf()
    Dependency.placebo -> arrayOf("--recurse-submodules")
    Dependency.freetype -> arrayOf()
    Dependency.harfbuzz -> arrayOf()
    Dependency.fribidi -> arrayOf()
    Dependency.mbedtls -> arrayOf("--recurse-submodules")
    Dependency.shaderc -> arrayOf()
    Dependency.moltenvk -> arrayOf()
    Dependency.uchardet -> arrayOf()
    Dependency.mpv -> arrayOf()
    Dependency.unibreak -> arrayOf()
    Dependency.dovi -> arrayOf()
    Dependency.spirvcross -> arrayOf()
    Dependency.lcms -> arrayOf()
  }

val Dependency.repoUrl: String
  get() = when (this) {
    Dependency.ffmpeg -> "https://github.com/FFmpeg/FFmpeg.git"
    Dependency.ass -> "https://github.com/libass/libass.git"
    Dependency.dav1d -> "https://code.videolan.org/videolan/dav1d.git"
    Dependency.placebo -> "https://code.videolan.org/videolan/libplacebo.git"
    Dependency.freetype -> "https://gitlab.freedesktop.org/freetype/freetype.git"
    Dependency.harfbuzz -> "https://github.com/harfbuzz/harfbuzz.git"
    Dependency.fribidi -> "https://github.com/fribidi/fribidi.git"
    Dependency.mbedtls -> "https://github.com/Mbed-TLS/mbedtls.git"
    Dependency.moltenvk -> "https://github.com/KhronosGroup/MoltenVK.git"
    Dependency.uchardet -> "https://gitlab.freedesktop.org/uchardet/uchardet"
    Dependency.mpv -> "https://github.com/mpv-player/mpv.git"
    Dependency.unibreak -> "https://github.com/adah1972/libunibreak"
    Dependency.dovi -> "https://github.com/quietvoid/dovi_tool"
    Dependency.lcms -> "https://github.com/mm2/Little-CMS"
    Dependency.shaderc -> "https://github.com/google/shaderc.git"
    Dependency.spirvcross -> "https://github.com/KhronosGroup/SPIRV-Cross"
  }

val Dependency.versionName: String
  get() = when (this) {
    Dependency.ffmpeg -> "n7.1.1"
    Dependency.ass -> "0.17.4"
    Dependency.dav1d -> "1.5.1"
    Dependency.placebo -> "v7.351.0"
    Dependency.freetype -> "VER-2-13-3"
    Dependency.harfbuzz -> "11.2.1"
    Dependency.fribidi -> "v1.0.16"
    Dependency.mbedtls -> "v3.6.1"
    Dependency.shaderc -> "v2024.3"
    Dependency.moltenvk -> "v1.3.0"
    Dependency.uchardet -> "v0.0.8"
    Dependency.mpv -> "v0.40.0"
    Dependency.unibreak -> "libunibreak_6_1"
    Dependency.dovi -> "libdovi-3.3.2"
    Dependency.lcms -> "lcms2.16"
    Dependency.spirvcross -> "vulkan-sdk-1.3.268.0"
  }

data class FrameworkCreationData(
  val frameworkName: String,
  val headers: List<String>,
  val binaryName: String,
  val excludeHeaders: List<String>,
)

val Dependency.frameworks: List<FrameworkCreationData>
  get() = buildList {
    when (this@frameworks) {
      Dependency.ffmpeg -> add(
        FrameworkCreationData(
          frameworkName = "Avcodec",
          binaryName = "libavcodec",
          headers = listOf("libavcodec"),
          excludeHeaders = listOf("xvmc", "vdpau", "qsv", "dxva2", "d3d11va", "d3d12va"),
        ),
        FrameworkCreationData(
          frameworkName = "Avdevice",
          binaryName = "libavdevice",
          headers = listOf("libavdevice"),
          excludeHeaders = listOf(),
        ),
        FrameworkCreationData(
          frameworkName = "Avfilter",
          binaryName = "libavfilter",
          headers = listOf("libavfilter"),
          excludeHeaders = listOf(),
        ),
        FrameworkCreationData(
          frameworkName = "Avformat",
          binaryName = "libavformat",
          headers = listOf("libavformat"),
          excludeHeaders = listOf(),
        ),
        FrameworkCreationData(
          frameworkName = "Avutil",
          binaryName = "libavutil",
          headers = listOf("libavutil"),
          excludeHeaders = listOf(
            "hwcontext_vulkan",
            "hwcontext_vdpau",
            "hwcontext_vaapi",
            "hwcontext_qsv",
            "hwcontext_opencl",
            "hwcontext_dxva2",
            "hwcontext_d3d11va",
            "hwcontext_d3d12va",
            "hwcontext_cuda"
          ),
        ),
        FrameworkCreationData(
          frameworkName = "Swresample",
          binaryName = "libswresample",
          headers = listOf("libswresample"),
          excludeHeaders = listOf(),
        ),
        FrameworkCreationData(
          frameworkName = "Swscale",
          binaryName = "libswscale",
          headers = listOf("libswscale"),
          excludeHeaders = listOf(),
        ),
      )
      Dependency.shaderc -> add(
        FrameworkCreationData(
          headers = listOf("glslang", "shaderc", "spirv-tools"),
          binaryName = "libshaderc_combined",
          frameworkName = "Shaderc_combined",
          excludeHeaders = emptyList(),
        )
      )
      Dependency.spirvcross -> return@buildList
      Dependency.ass -> add(
        FrameworkCreationData(
          headers = listOf("ass"),
          binaryName = "libass",
          frameworkName = "Ass",
          excludeHeaders = emptyList(),
        )
      )
      Dependency.dav1d -> add(
        FrameworkCreationData(
          headers = listOf("dav1d"),
          binaryName = "libdav1d",
          frameworkName = "Dav1d",
          excludeHeaders = emptyList(),
        ),
      )
      Dependency.placebo -> add(
        FrameworkCreationData(
          headers = listOf("libplacebo"),
          binaryName = "libplacebo",
          frameworkName = "Placebo",
          excludeHeaders = emptyList(),
        ),
      )
      Dependency.freetype -> add(
        FrameworkCreationData(
          headers = listOf("freetype2"),
          binaryName = "libfreetype",
          frameworkName = "Freetype",
          excludeHeaders = emptyList(),
        )
      )
      Dependency.harfbuzz -> add(
        FrameworkCreationData(
          headers = listOf("harfbuzz"),
          binaryName = "libharfbuzz",
          frameworkName = "Harfbuzz",
          excludeHeaders = emptyList(),
        )
      )
      Dependency.fribidi -> add(
        FrameworkCreationData(
          headers = listOf("fribidi"),
          binaryName = "libfribidi",
          frameworkName = "Fribidi",
          excludeHeaders = emptyList(),
        )
      )
      Dependency.mbedtls -> add(
        FrameworkCreationData(
          headers = listOf("mbedtls"),
          binaryName = "libmbedtls",
          frameworkName = "Mbedtls",
          excludeHeaders = emptyList(),
        )
      )
      Dependency.moltenvk -> return@buildList
      Dependency.uchardet -> add(
        FrameworkCreationData(
          headers = listOf("uchardet"),
          binaryName = "libuchardet",
          frameworkName = "Uchardet",
          excludeHeaders = emptyList(),
        )
      )
      Dependency.mpv -> add(
        FrameworkCreationData(
          headers = listOf("mpv"),
          binaryName = "libmpv",
          frameworkName = "Mpv",
          excludeHeaders = emptyList(),
        )
      )
      Dependency.unibreak -> add(
        FrameworkCreationData(
          headers = listOf("eastasianwidthdef.h", "graphmebreak.h", "linebreak.h", "linebreakdef.h", "unibreakbase.h", "unibreakdef.h", "wordbreak.h"),
          binaryName = "libunibreak",
          frameworkName = "Unibreak",
          excludeHeaders = emptyList(),
        )
      )
      Dependency.dovi -> add(
        FrameworkCreationData(
          headers = listOf("libdovi"),
          binaryName = "libdovi",
          frameworkName = "Dovi",
          excludeHeaders = emptyList(),
        )
      )
      Dependency.lcms -> add(
        FrameworkCreationData(
          headers = listOf("lcms2_plugin.h", "lcms2.h"),
          binaryName = "liblcms2",
          frameworkName = "Lcms2",
          excludeHeaders = emptyList(),
        )
      )
    }
  }

fun Dependency.frameworkExcludeHeaders(framework: String): List<String> {
  return when (this) {
    Dependency.ffmpeg -> when (framework) {
      "Avcodec" -> listOf("xvmc", "vdpau", "qsv", "dxva2", "d3d11va", "d3d12va")
      "Avutil" -> listOf()
      else -> emptyList()
    }

    else -> emptyList()
  }
}