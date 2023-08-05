package dev.zacsweers

import com.tickaroo.tikxml.converter.htmlescape.StringEscapeUtils
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import java.time.format.DateTimeFormatter
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

// https://www.zacsweers.dev/rss/

internal interface BlogApi {
  suspend fun main(): Feed

  companion object {
    fun create(client: HttpClient): BlogApi {
      return object : BlogApi {
        override suspend fun main(): Feed {
          return client.get("https://www.zacsweers.dev/rss").body()
        }
      }
    }
  }
}

@Serializable
@XmlSerialName("rss")
data class Feed(val channel: Channel) {
  @Serializable
  @XmlSerialName("channel")
  data class Channel(
    @XmlElement val itemList: List<Entry>,
    @XmlElement val title: String? = null,
    @XmlElement val description: String? = null
  ) {
    @Serializable
    @XmlSerialName("item")
    data class Entry(
      @XmlElement @Serializable(HtmlEscapeStringSerializer::class) val title: String,
      @XmlElement val link: String,
      @XmlElement @Serializable(InstantSerializer::class) val pubDate: Instant
    )
  }
}

internal object InstantSerializer : KSerializer<Instant> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

  override fun deserialize(decoder: Decoder): Instant {
    return java.time.Instant.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(decoder.decodeString()))
      .toKotlinInstant()
  }

  override fun serialize(encoder: Encoder, value: Instant) = throw NotImplementedError()
}

/**
 * A String TypeConverter that escapes and unescapes HTML characters directly from string. This one
 * uses apache 3 StringEscapeUtils borrowed from tikxml.
 */
object HtmlEscapeStringSerializer : KSerializer<String> {

  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("EscapedString", PrimitiveKind.STRING)

  override fun deserialize(decoder: Decoder): String {
    return StringEscapeUtils.unescapeHtml4(decoder.decodeString())
  }

  override fun serialize(encoder: Encoder, value: String) {
    encoder.encodeString(StringEscapeUtils.escapeHtml4(value))
  }
}

// suspend fun main() {
//  val client = OkHttpClient.Builder().build()
//  val api = BlogApi.create(client)
//  when (val result = api.main()) {
//    is ApiResult.Success -> {
//      println(result.value)
//      exitProcess(0)
//    }
//    is ApiResult.Failure -> {
//      when (result) {
//        is ApiResult.Failure.NetworkFailure -> error(result.error)
//        is ApiResult.Failure.UnknownFailure -> error(result.error)
//        else -> error("Unknown failure: $result")
//      }
//    }
//  }
// }
