/*
 * Copyright (C) 2020 Zac Sweers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.ksp)
  application
  alias(libs.plugins.spotless)
  alias(libs.plugins.moshix)
  alias(libs.plugins.kotlin.serialization)
}

moshi { enableSealed.set(true) }

val jdk = libs.versions.jdk.get().toInt()

tasks.withType<KotlinCompile>().configureEach {
  compilerOptions {
    jvmTarget.set(JvmTarget.fromTarget(jdk.toString()))
    freeCompilerArgs.add("-Xjsr305=strict")
    progressiveMode.set(true)
    optIn.add("kotlin.ExperimentalStdlibApi")
  }
}

java { toolchain { languageVersion.set(JavaLanguageVersion.of(jdk)) } }

application { mainClass.set("dev.zacsweers.UpdateReadmeKt") }

// Fat jar configuration to run this as a standalone jar
// Configuration borrowed from https://stackoverflow.com/a/49284432/3323598
tasks.named<Jar>("jar") {
  manifest { attributes(mapOf("Main-Class" to "dev.zacsweers.UpdateReadmeKt")) }
  from(
    provider {
      configurations.compileClasspath
        .get()
        .filter { it.exists() }
        .map { if (it.isDirectory()) it else zipTree(it) }
    }
  )
  duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

spotless {
  kotlin { ktfmt("0.44").googleStyle() }
  kotlinGradle { ktfmt("0.44").googleStyle() }
}

dependencies {
  implementation(libs.clikt)
  implementation(libs.coroutines)
  implementation(libs.eithernet)
  implementation(libs.okio)
  implementation(libs.okhttp)
  implementation(libs.retrofit)
  implementation(libs.retrofit.moshi)
  implementation(libs.moshi)
  implementation(libs.moshi.adapters)

  // XML serialization
  implementation(libs.tikxml.htmlescape)
  implementation(libs.xmlutil.core)
  implementation(libs.xmlutil.serialization)
  implementation(libs.retrofit.kotlinxSerialization)
}
