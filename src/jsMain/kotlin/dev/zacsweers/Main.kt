package dev.zacsweers

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Window
import org.jetbrains.skiko.wasm.onWasmReady

fun main() {
  onWasmReady {
    Window {
      // https://github.com/JetBrains/compose-multiplatform/issues/2186
      val fontFamilyResolver = createFontFamilyResolver()
      @Suppress("DEPRECATION")
      CompositionLocalProvider(
        LocalDensity provides Density(1.0f),
        LocalLayoutDirection provides LayoutDirection.Ltr,
        LocalViewConfiguration provides androidx.compose.ui.platform.DefaultViewConfiguration(Density(1.0f)),
        LocalInputModeManager provides InputModeManagerObject,
        LocalFontFamilyResolver provides fontFamilyResolver
      ) {
        ReadMe()
      }
    }
  }
}

private object InputModeManagerObject : InputModeManager {
  override val inputMode = InputMode.Keyboard
  @ExperimentalComposeUiApi override fun requestInputMode(inputMode: InputMode) = false
}
