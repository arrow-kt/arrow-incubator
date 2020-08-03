@file:Suppress("PropertyName")

// properties use SHOUT_CASE

plugins {
  id("maven-publish")
  id("base")
  id("org.jetbrains.dokka")
  id("org.jlleitschuh.gradle.ktlint")
  id("ru.vyarus.animalsniffer")
  id("com.android.library")
}

repositories {
  google()
  jcenter()
}

val DOC_CONF: String by project
val ANDROID_CONF: String by project

apply(from = DOC_CONF)
//apply(from = ANDROID_CONF)

android {
  compileSdkVersion(29)

  defaultConfig {
    minSdkVersion (21)
  }

  sourceSets["main"].java.srcDir("src/main/kotlin")
  sourceSets["test"].java.srcDir("src/test/kotlin")
  sourceSets["androidTest"].java.srcDir("src/androidTest/kotlin")
}

tasks.withType<Test> {
  useJUnitPlatform()
}

dependencies {
  implementation("io.arrow-kt:arrow-fx-coroutines:0.10.5")

//    implementation project(":arrow-fx")
//    implementation("io.arrow-kt:arrow-fx:0.10.5")
//    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$KOTLIN_VERSION")
//    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$ANDROID_LIFECYCLE_VERSION")

//    testImplementation project(":arrow-fx-test")
}
