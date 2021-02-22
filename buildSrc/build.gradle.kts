plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin")
    implementation("com.diffplug.spotless:spotless-plugin-gradle:5.10.2")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.15.0")
}
