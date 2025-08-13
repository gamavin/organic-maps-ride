plugins {
  id("com.android.library")
  id("org.jetbrains.kotlin.android")           // versi diatur di settings.gradle
  id("org.jetbrains.kotlin.plugin.parcelize")  // versi diatur di settings.gradle
}

val propCompileSdkVersion: String by project
val propMinSdkVersion: String by project

android {
  namespace = "com.bitchat.corebridge"
  compileSdk = propCompileSdkVersion.toInt()

  defaultConfig {
    minSdk = propMinSdkVersion.toInt()
  }

  // Kotlin & Java berada di folder yang sama: src/main/java
  sourceSets {
    getByName("main") {
      java.setSrcDirs(listOf("src/main/java"))
      kotlin.setSrcDirs(listOf("src/main/java"))
      manifest.srcFile("src/main/AndroidManifest.xml")
      res.setSrcDirs(emptyList<String>())
      assets.setSrcDirs(emptyList<String>())
    }
  }

  compileOptions {
    // Naikkan ke 17 biar konsisten, aman untuk Java sources southernstorm/*
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  kotlinOptions {
    jvmTarget = "17"
    // (biarkan argumen di bawah jika sebelumnya membantu mixed-mode Kotlin+Java)
    freeCompilerArgs = freeCompilerArgs + listOf(
      "-Xjava-source-roots=${file("src/main/java").absolutePath}"
    )
  }

  packaging {
    resources.excludes += setOf(
      "META-INF/AL2.0",
      "META-INF/LGPL2.1",
      "META-INF/*.kotlin_module"
    )
  }
}

dependencies {
  // Dasar Kotlin/Android
  implementation("org.jetbrains.kotlin:kotlin-stdlib")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
  implementation("androidx.core:core-ktx:1.13.1")
  implementation("androidx.annotation:annotation:1.9.1")

  // Komponen yang dipakai core BitChat
  implementation("com.google.code.gson:gson:2.11.0")
  implementation("org.bouncycastle:bcprov-jdk15on:1.70")
  implementation("com.google.crypto.tink:tink-android:1.12.0")
  implementation("org.lz4:lz4-java:1.8.0")

  // Secure storage (dipakai SecureIdentityStateManager)
  implementation("androidx.security:security-crypto:1.1.0-alpha06")
}
