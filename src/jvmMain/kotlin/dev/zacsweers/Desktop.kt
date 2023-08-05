package dev.zacsweers

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import com.mikepenz.markdown.Markdown

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
      val readmeUpdater = remember { ReadMeUpdater() }
      val markdown by produceState<String?>(null) { value = readmeUpdater.generateReadme() }
      if (markdown == null) {
        Column(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.Center,
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          CircularProgressIndicator()
          Text("Loading…", style = MaterialTheme.typography.titleLarge)
        }
      } else {
        val stateVertical = rememberScrollState(0)
        Box(Modifier.fillMaxSize()) {
          Box(Modifier.fillMaxSize().verticalScroll(stateVertical).padding(16.dp)) {
            Markdown(markdown!!)
          }

          VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(stateVertical)
          )
        }
      }
    }
  }
