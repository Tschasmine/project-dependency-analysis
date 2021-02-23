plugins {
    id("com.tschasmine.dependency.analysis.kotlin-common-conventions")

    application
}

application.applicationName = "analyzer"

dependencies {
    implementation(project(":backend"))
    implementation("io.github.livingdocumentation:dot-diagram:1.1")
    implementation("commons-cli:commons-cli:1.4")
}
