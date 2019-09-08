include(":canidropjetifier", ":sample", ":sample-dependency")

pluginManagement {
    repositories {
        maven { url = uri("canidropjetifier/build/repository") }
        gradlePluginPortal()
    }
}
