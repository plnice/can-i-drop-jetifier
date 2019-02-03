include(":plugin", ":sample")

pluginManagement {
    repositories {
        maven { url = uri("plugin/build/repository") }
        gradlePluginPortal()
    }
}
