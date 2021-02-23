plugins {
    id("org.jetbrains.kotlin.jvm")
    id("io.gitlab.arturbosch.detekt")
}

repositories {
    jcenter()
}

dependencies {
    constraints {
        implementation("org.apache.commons:commons-text:1.9")

        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    }

    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("org.codehaus.groovy:groovy-all:2.5.14")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.2")
    testImplementation("org.hamcrest:hamcrest:2.2")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.test {
    useJUnitPlatform()
}
