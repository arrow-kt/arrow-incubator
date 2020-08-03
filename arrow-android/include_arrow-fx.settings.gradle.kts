@file:Suppress("PropertyName")

// Add ARROW_FX_LIB={location} to ~/.gradle/gradle.properties with the location of the arrow-fx repository
val ARROW_FX_LIB: String by settings

if (file(ARROW_FX_LIB).exists()) {
  include("arrow-fx-coroutines")
  project(":arrow-fx-coroutines").projectDir = file("../../arrow-fx/arrow-fx-coroutines")

  include("arrow-fx")
  project(":arrow-fx").projectDir = file("../../arrow-fx/arrow-fx")
  include("arrow-fx-test")
  project(":arrow-fx-test").projectDir = file("../../arrow-fx/arrow-fx-test")

  gradle.beforeProject {
    properties.forEach {
      println("$it")
    }
    configurations.all {
      resolutionStrategy {
        dependencySubstitution {
          substitute(module("io.arrow-kt:arrow-fx-coroutines:")).with(project(":arrow-fx-coroutines"))
        }
      }
    }
  }
}
