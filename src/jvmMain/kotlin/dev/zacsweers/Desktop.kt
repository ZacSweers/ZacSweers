package dev.zacsweers

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import com.mikepenz.markdown.compose.LocalMarkdownColors
import com.mikepenz.markdown.compose.LocalMarkdownTypography
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography

fun main() =
  singleWindowApplication(
    title = "README",
    state =
      WindowState(
        width = 1000.dp,
        height = 800.dp,
        position = WindowPosition.Aligned(Alignment.Center)
      ),
  ) {
    MaterialTheme {
      CompositionLocalProvider(
        LocalMarkdownColors provides markdownColor(),
        LocalMarkdownTypography provides markdownTypography(),
      ) {
        ReadMe()
      }
    }
  }
