plugins {
    id("org.springframework.boot") version "3.5.9"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.spring") version "2.2.20"
    kotlin("plugin.jpa") version "2.2.20"
    kotlin("kapt") version "2.2.20"
    id("com.github.ben-manes.versions") version "0.53.0"
    id("org.jlleitschuh.gradle.ktlint") version "13.1.0"
}

group = "no.novari"

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.fintlabs.no/releases")
    }
    mavenLocal()
}

var fintModelResourceVersion = "0.5.0"
var fintResourceModelVersion = "3.21.10"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")

    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    kapt("org.springframework.boot:spring-boot-configuration-processor")

    implementation("org.apache.commons:commons-text:1.14.0")
    implementation(kotlin("reflect"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("no.novari:flyt-web-instance-gateway:1.2.0")
    implementation("no.novari:flyt-kafka:4.0.0")
    implementation("no.novari:flyt-web-resource-server:2.0.0")

    implementation("no.fintlabs:fint-model-resource:$fintModelResourceVersion")
    implementation("no.fint:fint-arkiv-resource-model-java:$fintResourceModelVersion")
    implementation("no.fint:fint-administrasjon-resource-model-java:$fintResourceModelVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.test {
    useJUnitPlatform()
}

tasks.named("check") {
    dependsOn("ktlintCheck")
}
