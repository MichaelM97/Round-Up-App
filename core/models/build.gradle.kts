import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id(Plugins.JAVA_LIBRARY)
    id(Plugins.KOTLIN)
}

java {
    sourceCompatibility = Versions.JAVA
    targetCompatibility = Versions.JAVA
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = Versions.JAVA.toString()
}
