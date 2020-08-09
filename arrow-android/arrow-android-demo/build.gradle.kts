@file:Suppress("PropertyName")

plugins {
  id("com.android.application")
  kotlin("android")
  kotlin("android.extensions")
  id("androidx.navigation.safeargs.kotlin")
}

apply(plugin = "androidx.navigation.safeargs.kotlin")

android {
  compileSdkVersion(29)
  buildToolsVersion = "29.0.2"
  defaultConfig {
    applicationId = "arrow.android.demo"
    minSdkVersion(21)
    targetSdkVersion(29)
    versionCode = 1
    versionName = "1.0"
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  sourceSets["main"].java.srcDir("src/main/kotlin")
  sourceSets["test"].java.srcDir("src/test/kotlin")
  sourceSets["androidTest"].java.srcDir("src/androidTest/kotlin")

  buildTypes {
    named("release") {
      isMinifyEnabled = false
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  kotlinOptions {
    jvmTarget = "${JavaVersion.VERSION_1_8}"
  }
}

repositories {
  google()
  jcenter()
}

val KOTLIN_VERSION: String by project
val LATEST_VERSION: String by project

dependencies {
//  implementation(project(":arrow-android-binding-core"))
  implementation("io.arrow-kt:arrow-android-binding-core:$LATEST_VERSION")

  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$KOTLIN_VERSION")
  implementation("androidx.appcompat:appcompat:1.2.0")
  implementation("androidx.core:core-ktx:1.3.1")
  implementation("androidx.fragment:fragment-ktx:1.2.5")
  implementation("androidx.recyclerview:recyclerview:1.1.0")
  implementation("com.google.android.material:material:1.2.0")
  implementation("androidx.constraintlayout:constraintlayout:2.0.0-rc1")
  implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.2.0")
  implementation("androidx.navigation:navigation-ui-ktx:2.3.0")
  implementation("androidx.navigation:navigation-fragment-ktx:2.3.0")
}

configurations.all {
  resolutionStrategy {
    dependencySubstitution {
      substitute(module("io.arrow-kt:arrow-android-binding-core:")).with(project(":arrow-android-binding-core"))
    }
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
}
