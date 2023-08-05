package dev.zacsweers

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.js.Js
import kotlin.js.Date
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import kotlinx.dom.appendText
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.w3c.dom.Document
import org.w3c.dom.parsing.DOMParser

actual fun httpClient(config: HttpClientConfig<*>.() -> Unit) =
  HttpClient(Js) {
    config(this)
  }

actual fun parseRfc1123DateTime(dateTime: String): Instant = Date(dateTime).toKotlinInstant()

@Composable
actual fun PlatformMarkdown(text: String) {
  Text(text)
}

/**
 * A String TypeConverter that escapes and unescapes HTML characters directly from string. This one
 * uses apache 3 StringEscapeUtils borrowed from tikxml.
 */
actual object HtmlEscapeStringSerializer : KSerializer<String> {

  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("EscapedString", PrimitiveKind.STRING)

  override fun deserialize(decoder: Decoder): String {
    return DOMParser().parseFromString(decoder.decodeString(), "text/html").toString()
  }

  override fun serialize(encoder: Encoder, value: String) {
    val escaped = Document().createElement("throwaway").appendText(value).innerHTML
    encoder.encodeString(escaped)
  }
}
