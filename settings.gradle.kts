enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "mpv-build-apple"

include("ffmpeg")
include("fribidi")
include("harfbuzz")
include("freetype")
include("ass")
include("dovi")
include("mbedtls")
include("dav1d")
include("uchardet")
include("placebo")
include("mpv")
include("unibreak")
include("vulkan")
include("spirvcross")
include("shaderc")
include("littlecms2")
