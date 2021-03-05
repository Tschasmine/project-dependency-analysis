plugins {
    id("com.tschasmine.dependency.analysis.kotlin-application-conventions")
}

dependencies {
    implementation("org.apache.commons:commons-text")
}

application {
    // Define the main class for the application.
    mainClass.set("com.tschasmine.dependency.analysis.app.AppKt")
}

version = "0.0.1-alpha"
