package fr.composeplayer.builds.apple.misc

sealed interface Platform {
  val arch: Architecture
  data class IOS(override val arch: Architecture) : Platform
  data class IosSimulator(override val arch: Architecture) : Platform
  data class MacOS(override val arch: Architecture) : Platform
}

enum class Architecture { arm64, x86_64 }

val Platform.name: String
  get() = when (this) {
    is Platform.IOS -> "ios"
    is Platform.IosSimulator -> "isimulator"
    is Platform.MacOS -> "macos"
  }

val Platform.mesonSubSystem: String
  get() = when (this) {
    is Platform.IOS -> "ios"
    is Platform.IosSimulator -> "ios-simulator"
    is Platform.MacOS -> "macos"
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
    is Platform.IOS -> "iPhoneOS"
    is Platform.IosSimulator -> "iPhoneSimulator"
    is Platform.MacOS -> "MacOSX"
  }

val Platform.cmakeSystemName: String
  get() = when (this) {
    is Platform.IOS, is Platform.IosSimulator -> "ios"
    is Platform.MacOS -> "Darwin"
  }

val Platform.host: String
  get() = when (this) {
    is Platform.IOS, is Platform.IosSimulator -> when (arch) {
      Architecture.arm64 -> "x86_64-ios-darwin"
      Architecture.x86_64 -> "arm64-ios-darwin"
    }
    is Platform.MacOS -> when (arch) {
      Architecture.arm64 -> "x86_64-apple-darwin"
      Architecture.x86_64 -> "arm64-apple-darwin"
    }
  }

val Platform.rustTarget: String
  get() = when (this) {
    is Platform.IOS -> "${arch.cpuFamily}-apple-ios"
    is Platform.IosSimulator -> "${arch.cpuFamily}-apple-ios-sim"
    is Platform.MacOS -> "${arch.cpuFamily}-apple-darwin"
  }