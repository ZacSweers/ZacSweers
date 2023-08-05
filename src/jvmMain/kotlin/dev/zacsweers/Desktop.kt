package dev.zacsweers

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication

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
    ReadMe()
  }
