package dev.zacsweers

import io.ktor.http.ContentType
import io.ktor.serialization.Configuration
import io.ktor.serialization.kotlinx.serialization
import nl.adaptivity.xmlutil.serialization.XML

/** Copy of the first-party xml extension. First-party one doesn't support non-jvm. */
fun Configuration.xml(format: XML, contentType: ContentType) {
  serialization(contentType, format)
}
