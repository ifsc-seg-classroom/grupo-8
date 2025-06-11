plugins {
    java
    id("org.springframework.boot") version "3.1.0"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Vault and related dependencies
    implementation("org.springframework.cloud:spring-cloud-starter-vault-config:4.1.0")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.apache.httpcomponents:httpclient:4.5.14")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2022.0.4")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}