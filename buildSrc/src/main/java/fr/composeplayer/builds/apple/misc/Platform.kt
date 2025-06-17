package fr.composeplayer.builds.apple.misc

enum class Platform { ios, isimulator, macos }
data class BuildTarget(val platform: Platform, val arch: Architecture)
enum class Architecture { arm64, x86_64 }

val Platform.mesonSubSystem: String
  get() = when (this) {
    Platform.ios -> "ios"
    Platform.isimulator -> "ios-simulator"
    Platform.macos -> "macos"
  }

val Architecture.cpuFamily: String
  get() = when (this) {
    Architecture.arm64 -> "aarch64"
    Architecture.x86_64 -> "x86_64"
  }

val Architecture.targetCpu: String
  get() = when (this) {
    Architecture.arm64 -> "arm64"
    Architecture.x86_64 -> "x86_64"
  }

val Platform.sdk: String
  get() = when (this) {
    Platform.ios -> "iPhoneOS"
    Platform.isimulator -> "iPhoneSimulator"
    Platform.macos -> "MacOSX"
  }

val Platform.cmakeSystemName: String
  get() = when (this) {
    Platform.ios, Platform.isimulator -> "iOS"
    Platform.macos -> "Darwin"
  }

val BuildTarget.host: String
  get() = when (this.platform) {
    Platform.ios, Platform.isimulator -> when (arch) {
      Architecture.arm64 -> "x86_64-ios-darwin"
      Architecture.x86_64 -> "arm64-ios-darwin"
    }
    Platform.macos -> when (arch) {
      Architecture.arm64 -> "x86_64-apple-darwin"
      Architecture.x86_64 -> "arm64-apple-darwin"
    }
  }

val BuildTarget.rustTarget: String
  get() = when (platform) {
    Platform.ios -> "${arch.cpuFamily}-apple-ios"
    Platform.isimulator -> "${arch.cpuFamily}-apple-ios-sim"
    Platform.macos -> "${arch.cpuFamily}-apple-darwin"
  }

val Platform.buildTag: String
  get() = when (this) {
    Platform.ios -> "ios"
    Platform.isimulator -> "iossim"
    Platform.macos -> "macos"
  }
