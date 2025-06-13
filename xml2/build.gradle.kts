plugins {
  kotlin("jvm") version "2.1.20"
}

group = "fr.composeplayer.builds.mpv"
version = "unspecified"

repositories {
  mavenCentral()
}

dependencies {
  testImplementation(kotlin("test"))
}

tasks.test {
  useJUnitPlatform()
}
kotlin {
  jvmToolchain(23)
}