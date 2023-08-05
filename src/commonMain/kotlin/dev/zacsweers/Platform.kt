package dev.zacsweers

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig

// Adapted from https://ktor.io/docs/http-client-engines.html
expect fun httpClient(config: HttpClientConfig<*>.() -> Unit = {}): HttpClient
