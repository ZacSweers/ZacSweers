package dev.zacsweers

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.path
import com.github.ajalt.mordant.animation.progressAnimation
import com.github.ajalt.mordant.markdown.Markdown
import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.Spinner
import java.nio.file.Path
import kotlin.system.exitProcess
import kotlinx.coroutines.runBlocking
import okio.FileSystem
import okio.Path.Companion.toOkioPath

class UpdateReadmeCommand : CliktCommand() {

  private val outputFile: Path by
    option("-o", help = "The README.md file to write").path().required()

  override fun run() {
    val t = Terminal(AnsiLevel.TRUECOLOR, interactive = true)

    val animation =
      t.progressAnimation {
        spinner(Spinner.Dots(TextColors.brightBlue))
        text("Generating README.md...")
        progressBar()
      }
    animation.start()
    val newReadMe = runBlocking { ReadMeUpdater().generateReadme() }
    animation.stop()
    FileSystem.SYSTEM.write(outputFile.toOkioPath()) { writeUtf8(newReadMe) }

    t.print(Markdown(newReadMe))

    // TODO why do I need to do this
    exitProcess(0)
  }
}

fun main(argv: Array<String>) {
  UpdateReadmeCommand().main(argv)
}
