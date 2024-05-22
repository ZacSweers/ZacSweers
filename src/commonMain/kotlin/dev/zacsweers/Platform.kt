package dev.zacsweers

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

// Adapted from https://ktor.io/docs/http-client-engines.html
expect fun httpClient(config: HttpClientConfig<*>.() -> Unit = {}): HttpClient

/** A String TypeConverter that escapes and unescapes HTML characters directly from string. */
expect object HtmlEscapeStringSerializer : KSerializer<String> {
  override val descriptor: SerialDescriptor

  override fun serialize(encoder: Encoder, value: String)

  override fun deserialize(decoder: Decoder): String
}
