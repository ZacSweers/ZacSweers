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
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.ksp)
  application
  alias(libs.plugins.spotless)
  alias(libs.plugins.moshix)
  alias(libs.plugins.kotlin.serialization)
}

moshi { enableSealed.set(true) }

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

application { mainClass.set("dev.zacsweers.UpdateReadmeKt") }

kotlin {
  // region KMP Targets
  jvm()
  // endregion

  sourceSets {
    commonMain {
      dependencies {
        implementation(libs.clikt)
        implementation(libs.coroutines)
        implementation(libs.okio)
        implementation(libs.xmlutil.core)
        implementation(libs.xmlutil.serialization)
      }
    }
    maybeCreate("jvmMain").apply {
      dependencies {
        implementation(libs.eithernet)
        implementation(libs.moshi)
        implementation(libs.moshi.adapters)
        implementation(libs.okhttp)
        implementation(libs.retrofit)
        implementation(libs.retrofit.moshi)
        // XML serialization
        implementation(libs.tikxml.htmlescape)
        implementation(libs.retrofit.kotlinxSerialization)
      }
    }
  }

  targets.withType<KotlinJvmTarget> {
    // Needed for 'application' plugin.
    withJava()
  }
}

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
