package dev.zacsweers

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import kotlin.system.exitProcess
import kotlinx.coroutines.runBlocking
import okio.FileSystem

class UpdateReadmeCommand : CliktCommand() {

  val outputFile by option("-o", help = "The README.md file to write").path().required()

  override fun run() {
    val newReadMe = runBlocking { ReadmeUpdater().generateReadme() }
    FileSystem.SYSTEM.write(outputFile) { writeUtf8(newReadMe) }

    // TODO why do I need to do this
    exitProcess(0)
  }
}

fun main(argv: Array<String>) {
  UpdateReadmeCommand().main(argv)
}
