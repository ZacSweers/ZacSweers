package dev.zacsweers

import com.tickaroo.tikxml.converter.htmlescape.StringEscapeUtils
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp
import java.util.concurrent.TimeUnit
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

actual fun httpClient(config: HttpClientConfig<*>.() -> Unit) =
  HttpClient(OkHttp) {
    config(this)

    engine {
      config {
        retryOnConnectionFailure(true)
        connectTimeout(0, TimeUnit.SECONDS)
      }
    }
  }

/**
 * A String TypeConverter that escapes and unescapes HTML characters directly from string. This one
 * uses apache 3 StringEscapeUtils borrowed from tikxml.
 */
actual object HtmlEscapeStringSerializer : KSerializer<String> {

  actual override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("EscapedString", PrimitiveKind.STRING)

  actual override fun deserialize(decoder: Decoder): String {
    return StringEscapeUtils.unescapeHtml4(decoder.decodeString())
  }

  actual override fun serialize(encoder: Encoder, value: String) {
    encoder.encodeString(StringEscapeUtils.escapeHtml4(value))
  }
}
