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
  lcms2,
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
      Dependency.lcms2 -> Unit
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
    Dependency.lcms2 -> arrayOf()
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
    Dependency.lcms2 -> "https://github.com/mm2/Little-CMS"
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
    Dependency.lcms2 -> "lcms2.16"
    Dependency.spirvcross -> "vulkan-sdk-1.3.268.0"
  }

data class FrameworkCreationData(
  val frameworkName: String,
  val excludeHeaders: List<String> = emptyList(),
  val dynamicOnly: Boolean = false,
)

val Dependency.frameworks: List<FrameworkCreationData>
  get() = buildList {
    when (this@frameworks) {
      Dependency.ffmpeg -> add(
        FrameworkCreationData(
          frameworkName = "Avcodec",
          excludeHeaders = listOf("xvmc", "vdpau", "qsv", "dxva2", "d3d11va", "d3d12va"),
        ),
        FrameworkCreationData("Avdevice"),
        FrameworkCreationData("Avfilter"),
        FrameworkCreationData("Avformat"),
        FrameworkCreationData(
          frameworkName = "Avutil",
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
        FrameworkCreationData("Swresample"),
        FrameworkCreationData("Swscale"),
      )
      Dependency.shaderc -> this += FrameworkCreationData("Shaderc_combined")
      Dependency.spirvcross -> return@buildList
      Dependency.ass -> this += FrameworkCreationData("Ass")
      Dependency.dav1d -> this += FrameworkCreationData("Dav1d")
      Dependency.placebo -> this += FrameworkCreationData("Placebo")
      Dependency.freetype -> this += FrameworkCreationData("Freetype")
      Dependency.harfbuzz -> this += FrameworkCreationData("Harfbuzz")
      Dependency.fribidi -> this += FrameworkCreationData("Fribidi")
      Dependency.mbedtls -> add(
        FrameworkCreationData(
          frameworkName = "Mbedtls",
          excludeHeaders = emptyList(),
        ),
        FrameworkCreationData(
          frameworkName = "Mbedx509",
          excludeHeaders = emptyList(),
        ),
        FrameworkCreationData(
          frameworkName = "Mbedcrypto",
          excludeHeaders = emptyList(),
        ),
        FrameworkCreationData(
          frameworkName = "Everest",
          excludeHeaders = emptyList(),
          dynamicOnly = true,
        ),
        FrameworkCreationData(
          frameworkName = "P256m",
          excludeHeaders = emptyList(),
          dynamicOnly = true,
        ),
      )
      Dependency.moltenvk -> return@buildList
      Dependency.uchardet -> this += FrameworkCreationData("Uchardet")
      Dependency.mpv -> this += FrameworkCreationData("Mpv")
      Dependency.unibreak -> this += FrameworkCreationData("Unibreak")
      Dependency.dovi -> this += FrameworkCreationData("Dovi")
      Dependency.lcms2 -> this += FrameworkCreationData("Lcms2")
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