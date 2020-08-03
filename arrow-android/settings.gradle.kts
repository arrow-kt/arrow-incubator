rootProject.name = "arrow-android-lib"

include("arrow-android-binding-core")

apply(from = "plugins.settings.gradle.kts")

// TODO: extract location of fx to a property in $HOME/gradle.properties
if (file("../../arrow-fx").exists()) {
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
