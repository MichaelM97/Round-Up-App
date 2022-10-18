import Dependencies.hilt

plugins {
    id(Plugins.ANDROID_LIBRARY)
    id(Plugins.KOTLIN_ANDROID)
    id(Plugins.KOTLIN_KAPT)
    id(Plugins.HILT)
}

android {
    namespace = "com.michaelmccormick.network"
    compileSdk = Versions.TARGET_SDK
    defaultConfig {
        minSdk = Versions.MIN_SDK
        targetSdk = Versions.TARGET_SDK
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = Versions.JAVA
        targetCompatibility = Versions.JAVA
    }
    kotlinOptions.jvmTarget = Versions.JAVA.toString()
    packagingOptions.resources.excludes.addAll(Configuration.EXCLUDED_PACKAGING_OPTIONS)
    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

kapt.correctErrorTypes = true

dependencies {
    hilt()
    implementation(Dependencies.Network.RETROFIT)
    implementation(Dependencies.Network.RETROFIT_MOSHI_CONVERTER)
    implementation(Dependencies.Network.MOSHI)
    kapt(Dependencies.Network.MOSHI_KOTLIN_CODEGEN)

    testImplementation(project(Modules.Core.TEST))
}
