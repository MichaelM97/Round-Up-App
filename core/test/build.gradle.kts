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

tasks.withType<Test> {
    useJUnitPlatform()
}

dependencies {
    api(platform(Dependencies.Test.JUNIT_5_BOM))
    api(Dependencies.Test.JUNIT_5_JUPITER)
    api(Dependencies.Test.JUNIT_5_PARAMS)
    api(Dependencies.Test.KOTLIN_TEST)
    api(Dependencies.Test.COROUTINES_TEST)
    api(Dependencies.Test.MOCKK)
}
