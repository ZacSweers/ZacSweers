package dev.zacsweers

import androidx.compose.runtime.Composable
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer

// Adapted from https://ktor.io/docs/http-client-engines.html
expect fun httpClient(config: HttpClientConfig<*>.() -> Unit = {}): HttpClient

/**
 * A String TypeConverter that escapes and unescapes HTML characters directly from string.
 */
expect object HtmlEscapeStringSerializer : KSerializer<String>