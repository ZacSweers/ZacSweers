package dev.zacsweers

import com.slack.eithernet.ApiResult
import com.slack.eithernet.retryWithExponentialBackoff
import com.squareup.moshi.Moshi
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.HttpException

class ReadmeUpdater {
  private val okHttpClient = OkHttpClient.Builder().build()

  suspend fun generateReadme(): String {
    return withContext(Dispatchers.IO) {
      val githubActivity = fetchGithubActivity(okHttpClient)
      val blogActivity = fetchBlogActivity(okHttpClient)

      createReadMe(githubActivity, blogActivity)
    }
  }

  private fun failureLogger(failure: ApiResult.Failure<*>) {
    if (failure is ApiResult.Failure.HttpFailure<*>) {
      val error = failure.error
      check(error is HttpException?)
      System.err.println("HttpException: $error\n${error?.response()?.errorBody()?.string()}")
    }
  }

  private fun fetchBlogActivity(client: OkHttpClient): List<ActivityItem> {
    val blogApi = BlogApi.create(client)

    return runBlocking {
      val result =
        retryWithExponentialBackoff(maxAttempts = 5, onFailure = ::failureLogger) { blogApi.main() }
      when (result) {
        is ApiResult.Success -> {
          result.value.channel.itemList
            .map { entry ->
              ActivityItem(text = "[${entry.title}](${entry.link})", timestamp = entry.pubDate)
            }
            .take(10)
        }
        else -> error("Could not load blog content: $result")
      }
    }
  }

  private fun fetchGithubActivity(client: OkHttpClient): List<ActivityItem> {
    val moshi = Moshi.Builder().build()
    val githubApi = GitHubApi.create(client, moshi)
    val result = runBlocking {
      retryWithExponentialBackoff(maxAttempts = 10, onFailure = ::failureLogger) {
        githubApi.getUserActivity("ZacSweers")
      }
    }
    return when (result) {
      is ApiResult.Success -> {
        result.value
          .filter { it.public }
          .mapNotNull { event ->
            when (val payload = event.payload) {
              UnknownPayload,
              null -> return@mapNotNull null
              is IssuesEventPayload -> {
                ActivityItem(
                  "${payload.action} issue [#${payload.issue.number}](${payload.issue.htmlUrl}) on ${event.repo?.markdownUrl()}: \"${payload.issue.title}\"",
                  event.createdAt
                )
              }
              is IssueCommentEventPayload -> {
                ActivityItem(
                  "commented on [#${payload.issue.number}](${payload.comment.htmlUrl}) in ${event.repo?.markdownUrl()}",
                  event.createdAt
                )
              }
              is PullRequestPayload -> {
                val action = if (payload.pullRequest.merged == true) "merged" else payload.action
                ActivityItem(
                  "$action PR [#${payload.number}](${payload.pullRequest.htmlUrl}) to ${event.repo?.markdownUrl()}: \"${payload.pullRequest.title}\"",
                  event.createdAt
                )
              }
              is CreateEvent -> {
                ActivityItem(
                  "created ${payload.refType}${payload.ref?.let { " `$it`" } ?: ""} on ${event.repo?.markdownUrl()}",
                  event.createdAt
                )
              }
              is DeleteEvent -> {
                ActivityItem(
                  "deleted ${payload.refType}${payload.ref?.let { " `$it`" } ?: ""} on ${event.repo?.markdownUrl()}",
                  event.createdAt
                )
              }
            }
          }
          .take(10)
      }
      else -> error("Could not load GitHub activity.")
    }
  }

  data class ActivityItem(val text: String, val timestamp: Instant) {
    override fun toString(): String {
      return "**${timestamp.atZone(ZoneId.of("America/New_York")).toLocalDate()}** — $text"
    }
  }
}
