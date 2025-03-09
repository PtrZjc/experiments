plugins {
    java
    id("org.springframework.boot") version "3.4.3"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.springframework.cloud.contract") version "4.2.0"
    id("maven-publish")
}

group = "pl.zajacp.contracts"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(23)
    }
    withSourcesJar()
}

repositories {
    mavenCentral()
}

extra["springCloudVersion"] = "2024.0.0"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.cloud:spring-cloud-starter-contract-verifier")
    testImplementation("io.rest-assured:spring-mock-mvc")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

contracts {
    baseClassForTests.set("pl.zajacp.contracts.producer.BaseContractTest")
    testFramework.set(org.springframework.cloud.contract.verifier.config.TestFramework.JUNIT5)
    // This publishes the stubs to Maven local
    publishStubsToScm {
        // Not needed for mavenLocal()
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(tasks.named("verifierStubsJar"))

            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
        }
    }
    repositories {
        mavenLocal()
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.contractTest {
    useJUnitPlatform()
}
