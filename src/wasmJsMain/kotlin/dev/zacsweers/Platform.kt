package dev.zacsweers

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.js.Js
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

actual fun httpClient(config: HttpClientConfig<*>.() -> Unit) = HttpClient(Js) { config(this) }

/**
 * A String TypeConverter that escapes and unescapes HTML characters directly from string. This one
 * uses apache 3 StringEscapeUtils borrowed from tikxml.
 */
actual object HtmlEscapeStringSerializer : KSerializer<String> {

  actual override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("EscapedString", PrimitiveKind.STRING)

  actual override fun deserialize(decoder: Decoder): String {
    return decoder.decodeString()
  }

  actual override fun serialize(encoder: Encoder, value: String) {
    encoder.encodeString(value)
  }
}

@JsModule("@js-joda/timezone") external object JsJodaTimeZoneModule
