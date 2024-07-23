package dev.zacsweers

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.path
import com.github.ajalt.mordant.animation.coroutines.CoroutineAnimator
import com.github.ajalt.mordant.animation.coroutines.animateInCoroutine
import com.github.ajalt.mordant.markdown.Markdown
import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.Spinner
import com.github.ajalt.mordant.widgets.progress.progressBar
import com.github.ajalt.mordant.widgets.progress.progressBarLayout
import com.github.ajalt.mordant.widgets.progress.spinner
import com.github.ajalt.mordant.widgets.progress.text
import java.nio.file.Path
import kotlin.system.exitProcess
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okio.FileSystem
import okio.Path.Companion.toOkioPath

class UpdateReadmeCommand : CliktCommand() {

  private val quiet: Boolean by
    option("-q", help = "Don't print the README.md to the terminal", envvar = "CI").flag()

  private val outputFile: Path by
    option("-o", help = "The README.md file to write").path().required()

  override fun run() {
    val t = Terminal(AnsiLevel.TRUECOLOR, interactive = true)

    val newReadMe = runBlocking {
      val progress: CoroutineAnimator? =
        if (!quiet) {
          progressBarLayout {
              spinner(Spinner.Dots(TextColors.brightBlue))
              text("Generating README.md...")
              progressBar()
            }
            .animateInCoroutine(t)
            .also { launch { it.execute() } }
        } else {
          null
        }
      ReadMeUpdater().generateReadme().also { progress?.stop() }
    }
    FileSystem.SYSTEM.write(outputFile.toOkioPath()) { writeUtf8(newReadMe) }

    if (!quiet) {
      t.print(Markdown(newReadMe))
    }

    // TODO why do I need to do this
    exitProcess(0)
  }
}

fun main(argv: Array<String>) {
  UpdateReadmeCommand().main(argv)
}
