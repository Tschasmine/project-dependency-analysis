plugins {
    id("com.diffplug.spotless")
}

repositories {
    jcenter()
}

spotless {
    kotlinGradle {
        ktlint()
    }
}
