plugins {
    id("com.tschasmine.dependency.analysis.kotlin-library-conventions")
}

dependencies {
    api("com.google.guava:guava:30.1-jre")
    implementation("com.github.javaparser:javaparser-core:3.19.0")
}
