package fr.composeplayer.builds.apple.tasks

import fr.composeplayer.builds.apple.misc.Dependency
import fr.composeplayer.builds.apple.misc.cloneArgs
import fr.composeplayer.builds.apple.misc.repoUrl
import fr.composeplayer.builds.apple.misc.versionName
import fr.composeplayer.builds.apple.utils.execExpectingSuccess
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject

open class CloneTask @Inject constructor(
  private val operations: ExecOperations
) : DefaultTask() {

  @Input lateinit var dirName: String
  @Input lateinit var url: String
  @Input lateinit var branch: String
  @Input lateinit var gitArgs: Array<String>

  @TaskAction
  fun execute() {
    val vendorDir = File(project.rootDir, "vendor").apply(File::mkdirs)
    try {
      val exists = vendorDir.resolve(dirName).exists()
      if (exists) {
        logger.info("Skipping clone for component [$dirName]")
        return
      }
      logger.info("Cloning component [$dirName]")
      operations.execExpectingSuccess {
        workingDir = vendorDir
        command = arrayOf("git", "clone", "--depth", "1", "--branch", branch, *gitArgs, url, dirName)
      }
      val patchDir = project.rootDir.resolve("patches/$dirName")
      if ( !patchDir.exists() ) {
        return
      }
      val patches = patchDir.listFiles()!!
      for (patch in patches) {
        if (patch.extension != "patch") continue
        logger.lifecycle("Adding patch $patch to component [$dirName]")
        execExpectingSuccess {
          workingDir = vendorDir.resolve(dirName)
          command = arrayOf("git", "apply", patch.absolutePath)
        }
      }
    } catch (error: Throwable) {
      vendorDir.resolve(dirName).deleteRecursively()
      throw error
    }

  }

}

fun CloneTask.applyFrom(dep: Dependency) {
  this.dirName = dep.name
  this.url = dep.repoUrl
  this.branch = dep.versionName
  this.gitArgs = dep.cloneArgs
}