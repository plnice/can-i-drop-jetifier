include(":canidropjetifier", ":sample")

pluginManagement {
    repositories {
        maven { url = uri("canidropjetifier/build/repository") }
        gradlePluginPortal()
    }
}
