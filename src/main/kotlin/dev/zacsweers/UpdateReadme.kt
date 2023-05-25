package dev.zacsweers

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import com.slack.eithernet.ApiResult
import com.slack.eithernet.ApiResult.Failure.HttpFailure
import com.slack.eithernet.ApiResult.Success
import com.slack.eithernet.retryWithExponentialBackoff
import com.squareup.moshi.Moshi
import com.tickaroo.tikxml.TikXml
import java.time.Instant
import java.time.ZoneId
import kotlin.system.exitProcess
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import retrofit2.HttpException

class UpdateReadmeCommand : CliktCommand() {

  val outputFile by option("-o", help = "The README.md file to write").file().required()

  override fun run() {
    val okHttpClient = OkHttpClient.Builder().build()

    val githubActivity = fetchGithubActivity(okHttpClient)
    val blogActivity = fetchBlogActivity(okHttpClient)

    val newReadMe = createReadMe(githubActivity, blogActivity)
    outputFile.writeText(newReadMe)

    // TODO why do I need to do this
    exitProcess(0)
  }
}

private fun failureLogger(failure: ApiResult.Failure<*>) {
  if (failure is HttpFailure<*>) {
    val error = failure.error
    check(error is HttpException?)
    System.err.println("HttpException: $error\n${error?.response()?.errorBody()?.string()}")
  }
}

private fun fetchBlogActivity(client: OkHttpClient): List<ActivityItem> {
  val blogApi =
    BlogApi.create(
      client,
      TikXml.Builder()
        .exceptionOnUnreadXml(false)
        .addTypeConverter(Instant::class.java, InstantTypeConverter())
        .build()
    )

  return runBlocking {
    val result =
      retryWithExponentialBackoff(maxAttempts = 5, onFailure = ::failureLogger) { blogApi.main() }
    when (result) {
      is Success -> {
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
    is Success -> {
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

fun main(argv: Array<String>) {
  UpdateReadmeCommand().main(argv)
}

data class ActivityItem(val text: String, val timestamp: Instant) {
  override fun toString(): String {
    return "**${timestamp.atZone(ZoneId.of("America/New_York")).toLocalDate()}** — $text"
  }
}
