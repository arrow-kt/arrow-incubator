@file:Suppress("PropertyName", "LocalVariableName")

// Add ARROW_FX_LIB={location} to ~/.gradle/gradle.properties with the location of the arrow-fx repository
val ARROW_FX_LIB: String? by settings

if (ARROW_FX_LIB?.let { file(it) }?.exists() == true) {

  include("arrow-fx-coroutines")
  project(":arrow-fx-coroutines").projectDir = file("../../arrow-fx/arrow-fx-coroutines")

  include("arrow-fx")
  project(":arrow-fx").projectDir = file("../../arrow-fx/arrow-fx")
  include("arrow-fx-test")
  project(":arrow-fx-test").projectDir = file("../../arrow-fx/arrow-fx-test")

  val ROOT_PROPERTIES: String by settings
  val rootProperties = java.util.Properties().apply { load(java.net.URL(ROOT_PROPERTIES).openStream()) }
  val ATOMICFU_VERSION: String by rootProperties

  gradle.rootProject {
    buildscript {
      dependencies {
        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:$ATOMICFU_VERSION")
      }
    }
  }

  gradle.beforeProject {
    if ("arrow-fx" in name) apply(plugin = "kotlinx-atomicfu")
    configurations.all {
      resolutionStrategy {
        dependencySubstitution {
          substitute(module("io.arrow-kt:arrow-fx-coroutines:")).with(project(":arrow-fx-coroutines"))
        }
      }
    }
  }

}
