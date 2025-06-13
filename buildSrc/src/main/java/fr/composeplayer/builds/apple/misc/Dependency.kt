package fr.composeplayer.builds.apple.misc

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

val Dependency.requirements: List<Dependency>
  get() = when (this) {
    Dependency.ffmpeg -> listOf(Dependency.mbedtls, Dependency.dav1d)
    Dependency.ass -> listOf(Dependency.fribidi, Dependency.unibreak, Dependency.harfbuzz, Dependency.uchardet)
    Dependency.dav1d -> listOf()
    Dependency.placebo -> listOf(Dependency.dovi, Dependency.lcms, Dependency.shaderc, Dependency.moltenvk, Dependency.spirvcross)
    Dependency.freetype -> listOf(Dependency.harfbuzz)
    Dependency.harfbuzz -> listOf()
    Dependency.fribidi -> listOf()
    Dependency.mbedtls -> listOf()
    Dependency.uchardet -> listOf()
    Dependency.unibreak -> listOf()
    Dependency.dovi -> listOf()
    Dependency.lcms -> listOf()
    Dependency.mpv -> listOf(Dependency.ffmpeg, Dependency.ass)
    Dependency.spirvcross -> listOf(Dependency.moltenvk)
    Dependency.shaderc -> listOf(Dependency.moltenvk)
    Dependency.moltenvk -> listOf()
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
    Dependency.shaderc -> "v2025.2"
    Dependency.moltenvk -> "v1.3.0"
    Dependency.uchardet -> "v0.0.8"
    Dependency.mpv -> "v0.40.0"
    Dependency.unibreak -> "libunibreak_6_1"
    Dependency.dovi -> "libdovi-3.3.2"
    Dependency.lcms -> "lcms2.16"
    Dependency.spirvcross -> "vulkan-sdk-1.3.268.0"
  }