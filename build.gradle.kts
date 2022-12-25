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
  kotlin("jvm") version "1.8.0-RC2"
  kotlin("kapt") version "1.8.0-RC2"
  id("com.google.devtools.ksp") version "1.8.0-RC2-1.0.8"
  `application`
  id("com.diffplug.spotless") version "6.12.0"
  id("com.github.ben-manes.versions") version "0.44.0"
  id("dev.zacsweers.moshix") version "0.20.0-1.8.0-Beta01"
}

moshi {
  enableSealed.set(true)
}

tasks.withType<KotlinCompile>().configureEach {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_17)
    freeCompilerArgs.addAll(
      "-Xjsr305=strict",
      "-progressive",
      "-opt-in=kotlin.ExperimentalStdlibApi"
    )
  }
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(17))
  }
}

sourceSets {
  main {
    java {
      srcDir("build/generated/source/kapt/main")
    }
  }
}

configure<JavaApplication> {
  mainClass.set("dev.zacsweers.UpdateReadmeKt")
}

// Fat jar configuration to run this as a standalone jar
// Configuration borrowed from https://stackoverflow.com/a/49284432/3323598
tasks.named<Jar>("jar") {
  manifest {
    attributes(
      mapOf(
        "Main-Class" to "dev.zacsweers.UpdateReadmeKt"
      )
    )
  }
  from(provider {
    configurations.compileClasspath.get().filter { it.exists() }
      .map { if (it.isDirectory()) it else zipTree(it) }
  })
  duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

dependencies {
  kapt("com.tickaroo.tikxml:processor:0.8.13")

  implementation("com.github.ajalt:clikt:2.8.0")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
  implementation("com.squareup.okio:okio:3.2.0")
  implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.11")
  implementation("com.squareup.retrofit2:retrofit:2.9.0")
  implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
  implementation("com.squareup.moshi:moshi:1.14.0")
  implementation("com.squareup.moshi:moshi-adapters:1.14.0")

  // XML serialization
  implementation("com.tickaroo.tikxml:annotation:0.8.13")
  implementation("com.tickaroo.tikxml:core:0.8.13")
  implementation("com.tickaroo.tikxml:retrofit-converter:0.8.13")
  implementation("com.tickaroo.tikxml:converter-htmlescape:0.8.13")
}
