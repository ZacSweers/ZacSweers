package dev.zacsweers

import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import nl.adaptivity.xmlutil.serialization.XML

class ReadMeUpdater {

  private val client = httpClient {
    install(HttpRequestRetry) {
      retryOnExceptionOrServerErrors(maxRetries = 2)
      exponentialDelay()
    }
    install(ContentNegotiation) {
      json(json = Json { ignoreUnknownKeys = true })
      xml(
        format = XML { defaultPolicy { ignoreUnknownChildren() } },
        contentType = ContentType.Text.Xml,
      )
    }
  }

  suspend fun generateReadme(): String {
    return withContext(Dispatchers.Default) {
      val githubActivity = async { fetchGithubActivity() }
      val blogActivity = async { fetchBlogActivity() }

      // Fetch in parallel
      createReadMe(githubActivity.await(), blogActivity.await())
    }
  }

  private suspend fun fetchBlogActivity(): List<ActivityItem> {
    val blogApi = BlogApi.create(client)

    val feed =
      try {
        blogApi.main()
      } catch (e: Exception) {
        println("Could not load blog content.")
        e.printStackTrace()
        return listOf(
          ActivityItem("Could not load blog content. Please check back later.", Clock.System.now())
        )
      }
    return feed.channel.items
      .map { entry ->
        ActivityItem(text = "[${entry.title}](${entry.link})", timestamp = entry.pubDate)
      }
      .take(10)
  }

  private suspend fun fetchGithubActivity(): List<ActivityItem> {
    val githubApi = GitHubApi.create(client)
    val events =
      try {
        githubApi.getUserActivity("ZacSweers")
      } catch (e: Exception) {
        println("Could not load GitHub activity.")
        e.printStackTrace()
        return listOf(
          ActivityItem(
            "Could not load GitHub activity. Please check back later.",
            Clock.System.now(),
          )
        )
      }
    return events
      .filter { it.public }
      .mapNotNull { event ->
        when (val payload = event.payload) {
          UnknownPayload,
          null -> return@mapNotNull null
          is IssuesEventPayload -> {
            ActivityItem(
              "${payload.action} issue [#${payload.issue.number}](${payload.issue.htmlUrl}) on ${event.repo?.markdownUrl()}: \"${payload.issue.title}\"",
              event.createdAt,
            )
          }
          is IssueCommentEventPayload -> {
            ActivityItem(
              "commented on [#${payload.issue.number}](${payload.comment.htmlUrl}) in ${event.repo?.markdownUrl()}",
              event.createdAt,
            )
          }
          is PullRequestPayload -> {
            val action = if (payload.pullRequest.merged == true) "merged" else payload.action
            ActivityItem(
              "$action PR [#${payload.number}](${payload.pullRequest.htmlUrl}) to ${event.repo?.markdownUrl()}: \"${payload.pullRequest.title}\"",
              event.createdAt,
            )
          }
          is CreateEvent -> {
            ActivityItem(
              "created ${payload.refType}${payload.ref?.let { " `$it`" } ?: ""} on ${event.repo?.markdownUrl()}",
              event.createdAt,
            )
          }
          is DeleteEvent -> {
            ActivityItem(
              "deleted ${payload.refType}${payload.ref?.let { " `$it`" } ?: ""} on ${event.repo?.markdownUrl()}",
              event.createdAt,
            )
          }
        }
      }
      .take(10)
  }

  data class ActivityItem(val text: String, val timestamp: Instant) {
    override fun toString() =
      "**${timestamp.toLocalDateTime(TimeZone.of("America/New_York")).date}** — $text"
  }
}
