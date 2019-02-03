plugins {
    `kotlin-dsl`
    `maven-publish`
    id("org.jetbrains.kotlin.plugin.allopen") version "1.3.20"
}

gradlePlugin {
    plugins {
        register("canidropjetifier") {
            id = "com.github.plnice.canidropjetifier"
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
version = "0.1"

publishing {
    repositories {
        maven(url = "build/repository")
    }
}
