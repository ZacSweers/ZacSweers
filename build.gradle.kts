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
import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  application
  alias(libs.plugins.spotless)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.compose)
}

val jdk = libs.versions.jdk.get().toInt()

tasks.withType<KotlinCompilationTask<*>>().configureEach {
  compilerOptions {
    progressiveMode.set(true)
    optIn.add("kotlin.ExperimentalStdlibApi")

    if (this is KotlinJvmCompilerOptions) {
      freeCompilerArgs.add("-Xjsr305=strict")
      jvmTarget.set(JvmTarget.fromTarget(jdk.toString()))
    }
  }
}

java { toolchain { languageVersion.set(JavaLanguageVersion.of(jdk)) } }

application { mainClass.set("dev.zacsweers.UpdateReadmeCommandKt") }

compose {
  kotlinCompilerPlugin.set(libs.compose.compiler.get().toString())
  experimental { web.application {} }
}

kotlin {
  // region KMP Targets
  jvm()
  js(IR) {
    browser()
    nodejs()
    binaries.executable()
  }
  // No M3 for wasm() yet
  //  wasm()
  // endregion

  sourceSets {
    commonMain {
      dependencies {
        // TODO consolidate runtime deps again after
        // https://github.com/JetBrains/compose-multiplatform/issues/3424
        //        implementation(compose.runtime)
        implementation(compose.material3)
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
      }
    }
    maybeCreate("jvmMain").apply {
      dependencies {
        // To silence this stupid log https://www.slf4j.org/codes.html#StaticLoggerBinder
        implementation(libs.slf4jNop)
        implementation(compose.runtime)
        // https://github.com/ajalt/clikt/issues/438
        implementation(libs.clikt)
        // https://github.com/mikepenz/multiplatform-markdown-renderer/issues/55
        implementation(libs.compose.markdown)
        implementation(compose.desktop.currentOs)
        implementation(libs.okhttp)
        implementation(libs.ktor.client.engine.okhttp)
        implementation(libs.tikxml.htmlescape)
      }
    }
    maybeCreate("jsMain").apply {
      dependencies {
        implementation(compose.runtime)
        implementation(compose.html.core)
        implementation(compose.ui)
        implementation(compose.foundation)
        implementation(compose.material)
        @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
        implementation(compose.components.resources)
        implementation(libs.ktor.client.engine.js)
      }
    }
  }

  targets.withType<KotlinJvmTarget> {
    // Needed for 'application' plugin.
    withJava()
  }
}

tasks.withType<KotlinJsCompile>().configureEach {
  // https://github.com/JetBrains/compose-multiplatform/issues/3418
  kotlinOptions.freeCompilerArgs +=
    listOf(
      "-Xklib-enable-signature-clash-checks=false",
    )
}

// Fat jar configuration to run this as a standalone jar
// Configuration borrowed from https://stackoverflow.com/a/49284432/3323598
tasks.named<Jar>("jar") {
  manifest { attributes(mapOf("Main-Class" to "dev.zacsweers.UpdateReadmeCommandKt")) }
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

// a temporary workaround for a bug in jsRun invocation - see
// https://youtrack.jetbrains.com/issue/KT-48273
afterEvaluate {
  rootProject.extensions.configure<NodeJsRootExtension> {
    versions.webpackDevServer.version = "4.0.0"
    versions.webpackCli.version = "4.10.0"
  }
}

spotless {
  kotlin { ktfmt("0.44").googleStyle() }
  kotlinGradle { ktfmt("0.44").googleStyle() }
}
