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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.compose.LocalMarkdownColors
import com.mikepenz.markdown.compose.LocalMarkdownTypography
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography

@Composable
fun ReadMe() {
  MaterialTheme {
    CompositionLocalProvider(
      LocalMarkdownColors provides markdownColor(),
      LocalMarkdownTypography provides markdownTypography(),
    ) {
      ReadMeBody()
    }
  }
}

@Composable
private fun ReadMeBody() {
  val readmeUpdater = remember { ReadMeUpdater() }
  val markdown by produceState<String?>(null) { value = readmeUpdater.generateReadme() }
  if (markdown == null) {
    Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      CircularProgressIndicator()
      Text("Loading…", style = MaterialTheme.typography.titleLarge)
    }
  } else {
    val stateVertical = rememberScrollState(0)
    Box(Modifier.fillMaxSize()) {
      Box(Modifier.fillMaxSize().verticalScroll(stateVertical).padding(16.dp)) {
        Markdown(
          markdown!!,
          colors = LocalMarkdownColors.current,
          typography = LocalMarkdownTypography.current,
        )
      }

      VerticalScrollbar(
        modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
        adapter = rememberScrollbarAdapter(stateVertical),
      )
    }
  }
}
