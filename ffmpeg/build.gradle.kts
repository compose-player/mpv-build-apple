import fr.composeplayer.builds.apple.misc.Architecture
import fr.composeplayer.builds.apple.misc.Dependency
import fr.composeplayer.builds.apple.misc.Platform
import fr.composeplayer.builds.apple.misc.cpuFamily
import fr.composeplayer.builds.apple.tasks.args
import fr.composeplayer.builds.apple.tasks.buildContext
import fr.composeplayer.builds.apple.utils.DEFAULT_TARGETS
import fr.composeplayer.builds.apple.utils.add
import fr.composeplayer.builds.apple.utils.registerBasicWorkflow

plugins {
  kotlin("jvm")
}

group = "fr.composeplayer.builds.mpv"
version = libs.versions.library

repositories { mavenCentral() }
kotlin { jvmToolchain(23) }

afterEvaluate {

  val dependency = Dependency.ffmpeg

  registerBasicWorkflow(
    dependency = dependency,
    targets = DEFAULT_TARGETS,
    prebuild = { enabled = false },
    build = {
      this.args = buildList {
        addAll(ffmpegConfigurers)
        add(
          "--disable-debug",
          "--enable-stripping",
          "--enable-optimizations",
          "--disable-large-tests",
          "--ignore-tests=TESTS",
          "--arch=${buildTarget.get().arch.cpuFamily}",
          "--target-os=darwin",
        )

        if (buildTarget.get().arch == Architecture.x86_64) {
          add("--disable-neon", "--disable-asm")
        } else {
          add("--enable-neon", "--enable-asm")
        }
        add("--disable-programs")


        val dependencyLibrary = listOf(
          "mbedtls",
          "libfreetype",
          "libharfbuzz",
          "libfribidi",
          "libass",
          "vulkan",
          "libshaderc",
          "lcms2",
          "libplacebo",
          "libdav1d",
        )

        for (dep in dependencyLibrary) add("--enable-$dep")

        add("--enable-decoder=dav1d")
        add("--enable-filter=ass")
        add("--enable-filter=subtitles")
        add("--enable-filter=libplacebo")

      }
      doFirst {
        val context = buildContext(dependency, buildTarget.get())
        val videotoolbox = context.sourceDir.resolve("libavcodec/videotoolbox.c")
        videotoolbox.readLines()
          .toMutableList()
          .apply {
            this[791 - 1] = this[791 - 1].replace("kCVPixelBufferOpenGLESCompatibilityKey", "kCVPixelBufferMetalCompatibilityKey")
            this[793 - 1] = this[793 - 1].replace("kCVPixelBufferIOSurfaceOpenGLTextureCompatibilityKey", "kCVPixelBufferMetalCompatibilityKey")
          }
          .joinToString("\n")
          .apply(videotoolbox::writeText)
      }

    },
    postBuild = { buildTarget ->
      doLast {
        val context = buildContext(dependency, buildTarget)
        val includeDir = context.prefixDir.resolve("include")
        context.buildDir.resolve("config.h").apply {
          copyTo(target = includeDir.resolve("libavutil/config.h"), overwrite = true)
          copyTo(target = includeDir.resolve("libavcodec/config.h"), overwrite = true)
          copyTo(target = includeDir.resolve("libavformat/config.h"), overwrite = true)
        }
        val avutilHeaders = listOf("getenv_utf8.h", "libm.h", "thread.h", "intmath.h", "mem_internal.h", "attributes_internal.h")
        for (header in avutilHeaders) {
          val file = context.buildDir.resolve("src/libavutil/$header")
          val destination = includeDir.resolve("libavutil/$header")
          file.copyTo(destination)
        }
        context.buildDir.resolve("src/libavcodec/mathops.h").copyTo(target = includeDir.resolve("libavcodec/mathops.h"), overwrite = true)
        context.buildDir.resolve("src/libavformat/os_support.h").copyTo(target = includeDir.resolve("libavformat/os_support.h") )
        context.buildDir.resolve("src/libavutil/internal.h").copyTo(target = includeDir.resolve("libavutil/internal.h"), overwrite = true)

        includeDir.resolve("libavutil/internal.h")
          .apply {
            val text = this.readText()
              .replace("#include \"timer.h\"", "//#include \"timer.h\"")
              .replace("kCVPixelBufferIOSurfaceOpenGLTextureCompatibilityKey", "kCVPixelBufferMetalCompatibilityKey")
            writeText(text)
          }
      }

    },

  )


}

private val ffmpegConfigurers: List<String>
  get() = listOf(
    "--disable-armv5te", "--disable-armv6", "--disable-armv6t2",
    "--disable-bzlib", "--disable-gray", "--disable-iconv", "--disable-linux-perf",
    "--enable-shared", "--disable-small", "--disable-symver", "--disable-xlib",
    "--enable-cross-compile", "--disable-libxml2", "--enable-nonfree",
    "--enable-optimizations", "--enable-pic", "--enable-runtime-cpudetect", "--enable-static", "--enable-thumb", "--enable-version3",
    "--pkg-config-flags=--static",

    "--disable-doc", "--disable-htmlpages", "--disable-manpages", "--disable-podpages", "--disable-txtpages",

    "--enable-avcodec", "--enable-avformat", "--enable-avutil", "--enable-network", "--enable-swresample", "--enable-swscale",
    "--disable-devices", "--disable-outdevs", "--disable-indevs", "--disable-postproc",

    "--disable-d3d11va", "--disable-d3d12va", "--disable-dxva2", "--disable-vaapi", "--disable-vdpau",

    "--disable-muxers",
    "--enable-muxer=flac", "--enable-muxer=dash", "--enable-muxer=hevc",
    "--enable-muxer=m4v", "--enable-muxer=matroska", "--enable-muxer=mov", "--enable-muxer=mp4",
    "--enable-muxer=mpegts", "--enable-muxer='webm*'",

    "--disable-encoders",
    "--enable-encoder=aac", "--enable-encoder=alac", "--enable-encoder=flac", "--enable-encoder='pcm*'",
    "--enable-encoder=movtext", "--enable-encoder=mpeg4", "--enable-encoder=h264_videotoolbox",
    "--enable-encoder=hevc_videotoolbox", "--enable-encoder=prores", "--enable-encoder=prores_videotoolbox",

    "--enable-protocols",


    "--disable-demuxers",
    "--enable-demuxer=aac", "--enable-demuxer=ac3", "--enable-demuxer=aiff", "--enable-demuxer=amr",
    "--enable-demuxer=ape", "--enable-demuxer=asf", "--enable-demuxer=ass", "--enable-demuxer=av1",
    "--enable-demuxer=avi", "--enable-demuxer=caf", "--enable-demuxer=concat",
    "--enable-demuxer=dash", "--enable-demuxer=data", "--enable-demuxer=dv",
    "--enable-demuxer=eac3",
    "--enable-demuxer=flac", "--enable-demuxer=flv", "--enable-demuxer=h264", "--enable-demuxer=hevc",
    "--enable-demuxer=hls", "--enable-demuxer=live_flv", "--enable-demuxer=loas", "--enable-demuxer=m4v",

    "--enable-demuxer=matroska", "--enable-demuxer=mov", "--enable-demuxer=mp3", "--enable-demuxer='mpeg*'",
    "--enable-demuxer=ogg", "--enable-demuxer=rm", "--enable-demuxer=rtsp", "--enable-demuxer=rtp",
    "--enable-demuxer=srt", "--enable-demuxer=webvtt",
    "--enable-demuxer=vc1", "--enable-demuxer=wav", "--enable-demuxer=webm_dash_manifest",

    "--enable-bsfs",

    "--disable-decoders",

    "--enable-decoder=av1", "--enable-decoder=dca", "--enable-decoder=dxv",
    "--enable-decoder=ffv1", "--enable-decoder=ffvhuff", "--enable-decoder=flv",
    "--enable-decoder=h263", "--enable-decoder=h263i", "--enable-decoder=h263p", "--enable-decoder=h264",
    "--enable-decoder=hap", "--enable-decoder=hevc", "--enable-decoder=huffyuv",
    "--enable-decoder=indeo5",
    "--enable-decoder=mjpeg", "--enable-decoder=mjpegb", "--enable-decoder='mpeg*'", "--enable-decoder=mts2",
    "--enable-decoder=prores",
    "--enable-decoder=mpeg4", "--enable-decoder=mpegvideo",
    "--enable-decoder=rv10", "--enable-decoder=rv20", "--enable-decoder=rv30", "--enable-decoder=rv40",
    "--enable-decoder=snow", "--enable-decoder=svq3",
    "--enable-decoder=tscc", "--enable-decoder=txd",
    "--enable-decoder=wmv1", "--enable-decoder=wmv2", "--enable-decoder=wmv3",
    "--enable-decoder=vc1", "--enable-decoder=vp6", "--enable-decoder=vp6a", "--enable-decoder=vp6f",
    "--enable-decoder=vp7", "--enable-decoder=vp8", "--enable-decoder=vp9",

    "--enable-decoder='aac*'", "--enable-decoder='ac3*'", "--enable-decoder='adpcm*'", "--enable-decoder='alac*'",
    "--enable-decoder='amr*'", "--enable-decoder=ape", "--enable-decoder=cook",
    "--enable-decoder=dca", "--enable-decoder=dolby_e", "--enable-decoder='eac3*'", "--enable-decoder=flac",
    "--enable-decoder='mp1*'", "--enable-decoder='mp2*'", "--enable-decoder='mp3*'", "--enable-decoder=opus",
    "--enable-decoder='pcm*'", "--enable-decoder=sonic",
    "--enable-decoder=truehd", "--enable-decoder=tta", "--enable-decoder=vorbis", "--enable-decoder='wma*'",

    "--enable-decoder=ass", "--enable-decoder=ccaption", "--enable-decoder=dvbsub", "--enable-decoder=dvdsub",
    "--enable-decoder=mpl2", "--enable-decoder=movtext",
    "--enable-decoder=pgssub", "--enable-decoder=srt", "--enable-decoder=ssa", "--enable-decoder=subrip",
    "--enable-decoder=xsub", "--enable-decoder=webvtt",

    "--disable-filters",
    "--enable-filter=aformat", "--enable-filter=amix", "--enable-filter=anull", "--enable-filter=aresample",
    "--enable-filter=areverse", "--enable-filter=asetrate", "--enable-filter=atempo", "--enable-filter=atrim",
    "--enable-filter=bwdif", "--enable-filter=delogo",
    "--enable-filter=equalizer", "--enable-filter=estdif",
    "--enable-filter=firequalizer", "--enable-filter=format", "--enable-filter=fps",
    "--enable-filter=hflip", "--enable-filter=hwdownload", "--enable-filter=hwmap", "--enable-filter=hwupload",
    "--enable-filter=idet", "--enable-filter=lenscorrection", "--enable-filter='lut*'", "--enable-filter=negate", "--enable-filter=null",
    "--enable-filter=overlay",
    "--enable-filter=palettegen", "--enable-filter=paletteuse", "--enable-filter=pan",
    "--enable-filter=rotate",
    "--enable-filter=scale", "--enable-filter=setpts", "--enable-filter=superequalizer",
    "--enable-filter=transpose", "--enable-filter=trim",
    "--enable-filter=vflip", "--enable-filter=volume",
    "--enable-filter=w3fdif",
    "--enable-filter=yadif",
    "--enable-filter=avgblur_vulkan", "--enable-filter=blend_vulkan", "--enable-filter=bwdif_vulkan",
    "--enable-filter=chromaber_vulkan", "--enable-filter=flip_vulkan", "--enable-filter=gblur_vulkan",
    "--enable-filter=hflip_vulkan", "--enable-filter=nlmeans_vulkan", "--enable-filter=overlay_vulkan",
    "--enable-filter=vflip_vulkan", "--enable-filter=xfade_vulkan",
  )