package dev.zacsweers

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlin.time.Instant
import kotlinx.datetime.format.DateTimeComponents.Formats.RFC_1123
import kotlinx.datetime.parse
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
          return client.get("https://www.zacsweers.dev/rss/").body()
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
    @XmlElement val items: List<Item>,
    @XmlElement val title: String? = null,
    @XmlElement val description: String? = null,
  ) {
    @Serializable
    @XmlSerialName("item")
    data class Item(
      @XmlElement @Serializable(HtmlEscapeStringSerializer::class) val title: String,
      @XmlElement val link: String,
      @XmlElement @Serializable(InstantSerializer::class) val pubDate: Instant,
    )
  }
}

internal object InstantSerializer : KSerializer<Instant> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

  override fun deserialize(decoder: Decoder) = Instant.parse(decoder.decodeString(), RFC_1123)

  override fun serialize(encoder: Encoder, value: Instant) = throw NotImplementedError()
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
