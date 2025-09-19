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

@file:OptIn(ExperimentalWasmDsl::class, ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.targets.js.npm.LockStoreTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.spotless)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.compose)
  alias(libs.plugins.kotlin.plugin.compose)
}

val jdk = libs.versions.jdk.get().toInt()

tasks.withType<KotlinCompilationTask<*>>().configureEach {
  compilerOptions {
    progressiveMode.set(true)
    optIn.addAll("kotlin.ExperimentalStdlibApi", "kotlin.time.ExperimentalTime")
    freeCompilerArgs.add("-Xexpect-actual-classes")

    if (this is KotlinJvmCompilerOptions) {
      freeCompilerArgs.add("-Xjsr305=strict")
      jvmTarget.set(JvmTarget.fromTarget(jdk.toString()))
    }
  }
}

java { toolchain { languageVersion.set(JavaLanguageVersion.of(jdk)) } }

kotlin {
  // region KMP Targets
  jvm { binaries { executable { mainClass.set("dev.zacsweers.UpdateReadmeCommandKt") } } }
  wasmJs {
    browser { commonWebpackConfig { outputFileName = "zacsweers-root.js" } }
    binaries.executable()
  }
  // endregion

  sourceSets {
    commonMain {
      dependencies {
        implementation(compose.runtime)
        implementation(compose.material3)
        implementation(libs.compose.markdown)
        implementation(libs.compose.markdown.m3)
        implementation(libs.kotlinx.coroutines)
        implementation(libs.kotlinx.datetime)
        implementation(libs.kotlinx.serialization.json)
        // https://github.com/pdvrieze/xmlutil/issues/157
        implementation(libs.kotlinx.serialization.xml.core)
        implementation(libs.kotlinx.serialization.xml.serialization)
        implementation(libs.ktor.client)
        implementation(libs.ktor.client.contentNegotiation)
        implementation(libs.ktor.serialization.json)
        implementation(libs.okio)
        implementation(libs.mordant.coroutines)
        implementation(libs.mordant.markdown)
      }
    }
    jvmMain {
      dependencies {
        // To silence this stupid log https://www.slf4j.org/codes.html#StaticLoggerBinder
        implementation(libs.slf4jNop)
        implementation(compose.runtime)
        // https://github.com/ajalt/clikt/issues/438
        implementation(libs.clikt)
        implementation(compose.desktop.currentOs)
        implementation(libs.okhttp)
        implementation(libs.ktor.client.engine.okhttp)
        implementation(libs.tikxml.htmlescape)
      }
    }
    maybeCreate("wasmJsMain").apply {
      dependencies {
        implementation(compose.runtime)
        implementation(compose.ui)
        implementation(compose.foundation)
        implementation(compose.material)
        implementation(compose.components.resources)
        implementation(npm("@js-joda/timezone", "2.18.2"))
      }
    }
  }
}

tasks.withType<KotlinJsCompile>().configureEach {
  // https://github.com/JetBrains/compose-multiplatform/issues/3418
  compilerOptions.freeCompilerArgs.add("-Xklib-enable-signature-clash-checks=false")
}

spotless {
  kotlin { ktfmt("0.54").googleStyle() }
  kotlinGradle { ktfmt("0.54").googleStyle() }
}

tasks.withType<LockStoreTask>().configureEach {
  inputFile.set(project.layout.projectDirectory.file("kotlin-js-store/package-lock.json"))
}
