include("arrow-android-binding-core")
include("arrow-android-demo")

// TODO: Remove when moving arrow-android out of arrow-incubator
listOf("arrow-android-binding-core", "arrow-android-demo").forEach { module ->
  // without this the previous include() use arrow-incubator as the root module
  project(":$module").projectDir = file(module)
}

apply(from = "plugins.settings.gradle.kts")
apply(from = "include_arrow-fx.settings.gradle.kts")
