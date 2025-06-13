import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  alias(libs.plugins.kotlin.dsl)
  alias(libs.plugins.kotlin.jvm)
}


repositories {
  mavenCentral()
}


java {
  val stringValue = libs.versions.jdk.get()
  val javaVersion = JavaVersion.toVersion(stringValue)
  sourceCompatibility = javaVersion
  targetCompatibility = javaVersion
}


tasks.withType<KotlinCompile> {
  val stringValue = libs.versions.jdk.get()
  compilerOptions.jvmTarget = JvmTarget.fromTarget(stringValue)
}