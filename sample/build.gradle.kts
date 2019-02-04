plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")

    id("com.github.plnice.canidropjetifier") version "0.2"
}

android {
    compileSdkVersion(28)
    defaultConfig {
        applicationId = "com.example.canidropjetifier"
        minSdkVersion(21)
        targetSdkVersion(28)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation(fileTree("libs").matching { include("*.jar") })
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.20")
    implementation("androidx.appcompat:appcompat:1.0.2")
    implementation("androidx.core:core-ktx:1.0.1")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")
    testImplementation("junit:junit:4.12")
    androidTestImplementation("androidx.test:runner:1.1.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.1.1")

    // Example obsolete dependencies
    implementation("com.squareup.leakcanary:leakcanary-android:1.6.3")
    implementation("com.android.support:cardview-v7:28.0.0")
}

canIDropJetifier {
    verbose = true
}
