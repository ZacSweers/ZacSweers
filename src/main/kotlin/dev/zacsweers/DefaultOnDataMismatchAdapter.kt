/*
 * Copyright (C) 2017 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.zacsweers

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import java.lang.reflect.Type

class DefaultOnDataMismatchAdapter<T> private constructor(
  private val delegate: JsonAdapter<T>,
  private val defaultValue: T
) : JsonAdapter<T?>() {
  override fun fromJson(reader: JsonReader): T? {
    // Use a peeked reader to leave the reader in a known state even if there's an exception.
    val result = reader.peekJson().use { peeked ->
      try {
        // Attempt to decode to the target type with the peeked reader.
        delegate.fromJson(peeked)
      } catch (e: JsonDataException) {
        defaultValue
      }
    }
    // Skip the value back on the reader, no matter the state of the peeked reader.
    reader.skipValue()
    return result
  }

  override fun toJson(writer: JsonWriter, value: T?) {
    delegate.toJson(writer, value)
  }

  companion object {
    fun <T> newFactory(type: Class<T>, defaultValue: T): Factory {
      return object : Factory {
        override fun create(
          requestedType: Type, annotations: Set<Annotation>, moshi: Moshi
        ): JsonAdapter<*>? {
          if (type != requestedType) return null
          val delegate: JsonAdapter<T> = moshi.nextAdapter(this, type, annotations)
          return DefaultOnDataMismatchAdapter(delegate, defaultValue)
        }
      }
    }
  }
}