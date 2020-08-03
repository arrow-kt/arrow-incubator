@file:Suppress("PropertyName") // properties use SHOUT_CASE


val ROOT_PROPERTIES: String by settings

val rootProperties = java.util.Properties().apply {
  load(java.net.URL(ROOT_PROPERTIES).openStream())
}

val KOTLIN_VERSION: String by rootProperties
val BUILD_TIME_TRACKER_VERSION: String by rootProperties
val DOKKA_VERSION: String by rootProperties
val KTLINT_GRADLE_VERSION: String by rootProperties
val ANIMALS_SNIFFER_VERSION: String by rootProperties
val ANDROID_TOOLS_BUILD_PLUGIN_VERSION: String by rootProperties

pluginManagement.repositories {
  google()
  jcenter()
  gradlePluginPortal()
}

pluginManagement.resolutionStrategy.eachPlugin {
  val id = requested.id.id
  when {
    id.startsWith("org.jetbrains.kotlin") -> useVersion(KOTLIN_VERSION)
    id.startsWith("com.android") -> useModule("com.android.tools.build:gradle:$ANDROID_TOOLS_BUILD_PLUGIN_VERSION")
    id == "net.rdrei.android.buildtimetracker" -> useVersion(BUILD_TIME_TRACKER_VERSION)
    id == "org.jetbrains.dokka" -> useVersion(DOKKA_VERSION)
    id == "org.jlleitschuh.gradle.ktlint" -> useVersion(KTLINT_GRADLE_VERSION)
    id == "ru.vyarus.animalsniffer" -> useVersion(ANIMALS_SNIFFER_VERSION)
  }
}
