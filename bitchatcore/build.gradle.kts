plugins {
  id("com.android.library")
  id("org.jetbrains.kotlin.android") version "1.9.24"
}

val propCompileSdkVersion: String by project
val propMinSdkVersion: String by project

android {
  namespace = "com.bitchat.corebridge"
  compileSdk = propCompileSdkVersion.toInt() // ambil dari gradle.properties root
  defaultConfig { minSdk = propMinSdkVersion.toInt() }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  kotlinOptions { jvmTarget = "11" }
  sourceSets {
    getByName("main") {
      java.srcDirs(
        // ambil semua source BitChat
        "../bitchat-android-0.8.2/app/src/main/java"
      )
      // exclude UI/Compose & onboarding & app entry
      java.exclude(
        "com/bitchat/android/ui/**",
        "com/bitchat/android/onboarding/**",
        "com/bitchat/android/MainActivity.kt",
        "com/bitchat/android/BitchatApplication.kt",
        "com/bitchat/android/core/ui/**"
      )
      // untuk library ini, kita tidak butuh resource dari BitChat
      res.srcDirs(emptyList<String>())
      assets.srcDirs(emptyList<String>())
      manifest.srcFile("src/main/AndroidManifest.xml")
    }
  }
  packaging {
    resources.excludes += setOf(
      "META-INF/AL2.0", "META-INF/LGPL2.1", "META-INF/*.kotlin_module"
    )
  }
}

dependencies {
  // minimum yang dibutuhkan layer "mesh"
  implementation("org.jetbrains.kotlin:kotlin-stdlib")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
  implementation("androidx.core:core-ktx:1.13.1")
  implementation("androidx.annotation:annotation:1.7.1")
  implementation("com.google.code.gson:gson:2.11.0")
  implementation("org.bouncycastle:bcprov-jdk15on:1.70")
  implementation("com.google.crypto.tink:tink-android:1.12.0")
  implementation("org.lz4:lz4-java:1.8.0")
}
