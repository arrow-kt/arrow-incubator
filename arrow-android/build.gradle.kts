@file:Suppress("LocalVariableName", "PropertyName") // properties use SHOUT_CASE

buildscript {
  val COMMON_SETUP: String by project
  apply(from = COMMON_SETUP)
}


plugins {
  id("maven-publish")
  id("base")
  id("org.jetbrains.kotlin.jvm")
  id("org.jetbrains.kotlin.kapt")
  id("net.rdrei.android.buildtimetracker")
  id("org.jetbrains.dokka")
  id("org.jlleitschuh.gradle.ktlint")
  id("ru.vyarus.animalsniffer") apply false
}

val GENERIC_CONF: String by project

apply(from = GENERIC_CONF)

subprojects {
  apply(plugin = "ru.vyarus.animalsniffer")
}
