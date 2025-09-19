package dev.zacsweers

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlin.time.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

interface GitHubApi {
  suspend fun getUserActivity(login: String): List<GitHubActivityEvent>

  companion object {
    fun create(client: HttpClient): GitHubApi {
      return object : GitHubApi {
        override suspend fun getUserActivity(login: String): List<GitHubActivityEvent> {
          return client
            .get("https://api.github.com/users/$login/events")
            .body<List<GitHubActivityEvent>>()
        }
      }
    }
  }
}

@Serializable(GitHubActivityEvent.Serializer::class)
data class GitHubActivityEvent(
  val id: String,
  val createdAt: Instant,
  val payload: GitHubActivityEventPayload?,
  val public: Boolean,
  val repo: Repo?,
) {
  object Serializer : KSerializer<GitHubActivityEvent> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("GitHubActivityEvent")

    override fun serialize(encoder: Encoder, value: GitHubActivityEvent) =
      throw NotImplementedError()

    override fun deserialize(decoder: Decoder): GitHubActivityEvent {
      val input = decoder as? JsonDecoder ?: error("Expected JsonDecoder for this deserializer")
      val tree = input.decodeJsonElement()
      val payloadType =
        tree.jsonObject["type"]?.let {
          try {
            input.json.decodeFromJsonElement<GitHubActivityEventPayload.Type>(it)
          } catch (_: Exception) {
            null
          }
        }
      val payloadValue = tree.jsonObject["payload"]
      val payload =
        if (payloadType != null && payloadValue != null) {
          input.json.decodeFromJsonElement(payloadType.serializer, payloadValue)
        } else {
          null
        }
      val id = tree.jsonObject["id"]?.jsonPrimitive?.content ?: error("No id found")
      val createdAt =
        tree.jsonObject["created_at"]?.let {
          input.json.decodeFromJsonElement(Instant.serializer(), it)
        } ?: error("No created_at found")
      val public =
        tree.jsonObject["public"]?.jsonPrimitive?.content?.toBoolean() ?: error("No public found")
      val repo =
        tree.jsonObject["repo"]?.let { input.json.decodeFromJsonElement(Repo.serializer(), it) }

      return GitHubActivityEvent(id, createdAt, payload, public, repo)
    }
  }
}

sealed interface GitHubActivityEventPayload {
  @Suppress("unused")
  @Serializable
  enum class Type(val serializer: KSerializer<out GitHubActivityEventPayload>) {
    @SerialName(value = "IssuesEvent") ISSUE(IssuesEventPayload.serializer()),
    @SerialName(value = "IssueCommentEvent") ISSUE_COMMENT(IssueCommentEventPayload.serializer()),
    @SerialName(value = "PullRequestEvent") PULL_REQUEST(PullRequestPayload.serializer()),
    @SerialName(value = "CreateEvent") CREATE_EVENT(CreateEvent.serializer()),
    @SerialName(value = "DeleteEvent") DELETE_EVENT(DeleteEvent.serializer()),
  }
}

data object UnknownPayload : GitHubActivityEventPayload

@Serializable
data class IssuesEventPayload(val action: String, val issue: Issue) : GitHubActivityEventPayload

@Serializable
data class Issue(
  val title: String,
  val body: String? = null,
  @SerialName(value = "html_url") val htmlUrl: String,
  val number: Int,
)

@Serializable
data class IssueCommentEventPayload(val action: String, val comment: Comment, val issue: Issue) :
  GitHubActivityEventPayload

@Serializable
data class Comment(@SerialName(value = "html_url") val htmlUrl: String, val body: String)

@Serializable
data class PullRequestPayload(
  val action: String,
  val number: Int,
  @SerialName(value = "pull_request") val pullRequest: PullRequest,
) : GitHubActivityEventPayload

@Serializable
data class PullRequest(
  @SerialName(value = "html_url") val htmlUrl: String,
  val title: String,
  val body: String?,
  val merged: Boolean? = false,
)

@Serializable
data class Repo(val name: String, val url: String) {
  fun adjustedUrl(): String {
    return url.replaceFirst("api.", "").replaceFirst("repos/", "")
  }

  fun markdownUrl(): String = "[$name](${adjustedUrl()})"
}

@Serializable
data class CreateEvent(val ref: String?, @SerialName(value = "ref_type") val refType: String) :
  GitHubActivityEventPayload

@Serializable
data class DeleteEvent(val ref: String?, @SerialName(value = "ref_type") val refType: String) :
  GitHubActivityEventPayload
