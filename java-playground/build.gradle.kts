plugins {
    id("java")
}

group = "pl.zajacp.playground"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(23))
    }
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.3")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:3.27.3")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(listOf("--enable-preview", "-Xlint:preview"))
    options.release.set(23)
}

// Also add this for the Java runtime
tasks.withType<JavaExec>().configureEach {
    jvmArgs("--enable-preview")
}

// For testing if needed
tasks.withType<Test>().configureEach {
    jvmArgs("--enable-preview")
}
