import Dependencies.hilt

plugins {
    id(Plugins.ANDROID_LIBRARY)
    id(Plugins.KOTLIN_ANDROID)
    id(Plugins.KOTLIN_KAPT)
    id(Plugins.HILT)
}

android {
    namespace = "com.michaelmccormick.data"
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
    implementation(project(Modules.NETWORK))
    implementation(project(Modules.Core.MODELS))
    implementation(project(Modules.Core.FACTORIES))

    hilt()
    implementation(Dependencies.Logging.TIMBER)

    testImplementation(project(Modules.Core.TEST))
}
