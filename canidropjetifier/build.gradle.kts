plugins {
    `kotlin-dsl`
    `maven-publish`
    id("org.jetbrains.kotlin.plugin.allopen") version "1.3.50"
    id("com.gradle.plugin-publish") version "0.10.1"
}

gradlePlugin {
    plugins {
        register("canidropjetifier") {
            id = "com.github.plnice.canidropjetifier"
            displayName = "Can I drop Jetifier?"
            description = "Checks whether there are any dependencies using support library instead of AndroidX artifacts."
            implementationClass = "com.github.plnice.canidropjetifier.CanIDropJetifierPlugin"
        }
    }
}

allOpen {
    annotation("com.github.plnice.canidropjetifier.AllOpen")
}

repositories {
    jcenter()
}

group = "com.github.plnice"
version = "0.5"

publishing {
    repositories {
        maven(url = "build/repository")
    }
}

pluginBundle {
    website = "https://github.com/plnice/can-i-drop-jetifier"
    vcsUrl = "https://github.com/plnice/can-i-drop-jetifier"
    tags = listOf("android", "jetifier")
}
