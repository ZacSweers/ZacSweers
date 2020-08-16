package dev.zacsweers

import com.tickaroo.tikxml.TikXml
import com.tickaroo.tikxml.TypeConverter
import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml
import com.tickaroo.tikxml.converter.htmlescape.HtmlEscapeStringConverter
import com.tickaroo.tikxml.retrofit.TikXmlConverterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.http.GET
import java.time.Instant
import java.time.format.DateTimeFormatter

// https://www.zacsweers.dev/rss/

internal interface BlogApi {
  @GET("/rss")
  suspend fun main(): Feed

  companion object {
    fun create(client: OkHttpClient, tikXml: TikXml): BlogApi {
      return Retrofit.Builder()
        .baseUrl("https://www.zacsweers.dev")
        .validateEagerly(true)
        .client(client)
        .addConverterFactory(TikXmlConverterFactory.create(tikXml))
        .build()
        .create()
    }
  }
}

@Xml
data class Feed(
  @Element
  val channel: Channel
)

@Xml
data class Channel(
  @Element
  val itemList: List<Entry>,

  @PropertyElement
  val title: String? = null,

  @PropertyElement
  val description: String? = null
)

@Xml(name = "item")
data class Entry(
  @PropertyElement(converter = HtmlEscapeStringConverter::class)
  val title: String,
  @PropertyElement
  val link: String,
  @PropertyElement(converter = InstantTypeConverter::class)
  val pubDate: Instant
)

internal class InstantTypeConverter : TypeConverter<Instant> {
  override fun write(value: Instant): String = TODO("Unsupported")

  override fun read(value: String): Instant {
    return Instant.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(value))
  }
}
