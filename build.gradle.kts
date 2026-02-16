import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.gradle.kotlin.dsl.named

plugins {
    id("org.springframework.boot") version "3.5.10"
    id("io.spring.dependency-management") version "1.1.7"
    java
    id("com.github.ben-manes.versions") version "0.53.0"
}

group = "no.novari"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

configurations {
    compileOnly {
        extendsFrom(named("annotationProcessor").get())
    }
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.fintlabs.no/releases")
    }
    mavenCentral()
}

var fintModelResourceVersion = "0.5.0"
var fintResourceModelVersion = "3.21.10"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")

    compileOnly("org.projectlombok:lombok")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")

    implementation("org.apache.commons:commons-text:1.14.0")

    implementation("no.novari:flyt-instance-gateway:8.0.0")
    implementation("no.novari:flyt-kafka:6.0.0")
    implementation("no.novari:flyt-resource-server:7.0.0")

    implementation("no.fintlabs:fint-model-resource:$fintModelResourceVersion")
    implementation("no.fint:fint-arkiv-resource-model-java:$fintResourceModelVersion")
    implementation("no.fint:fint-administrasjon-resource-model-java:$fintResourceModelVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.test {
    useJUnitPlatform()
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return !isStable
}

tasks.named<DependencyUpdatesTask>("dependencyUpdates") {
    rejectVersionIf {
        isNonStable(candidate.version)
    }
}
