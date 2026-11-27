import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    dependencies {
        constraints {
            classpath(libs.commons.lang3) {
                because("Fixes CVE-2025-48924 in spring-boot-buildpack-platform")
            }
        }
    }
}

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.jpa)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.ktlint)
    jacoco
}

group = "no.novari"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

// Dependabot resolves with JDK 21; an explicit release keeps JVM 25 as the resolution target so gradle.lockfile gets updated.
tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.fintlabs.no/releases")
    }
    mavenLocal()
}

tasks.jar {
    isEnabled = false
}

dependencyLocking {
    lockAllConfigurations()
}

dependencies {
    // A platform instead of the dependency-management plugin, which puts Boot's Kotlin on ktlint's classpath.
    implementation(platform(libs.spring.boot.dependencies))

    constraints {
        ktlint(libs.logback.core) {
            because(
                "ktlint 1.8.0 brings logback-core 1.3.16, which has CVE-2026-1225, CVE-2026-9828 and CVE-2026-10532. " +
                    "Only the ktlint tool classpath",
            )
        }
    }

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.springframework.boot:spring-boot-starter-security-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-restclient")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("tools.jackson.module:jackson-module-kotlin")

    implementation(libs.flyt.gateway.starter)
    implementation(libs.flyt.cache)
    implementation(libs.fint.model.resource)
    implementation(libs.fint.arkiv.resource.model)
    implementation(libs.fint.administrasjon.resource.model)
    // For compatibility with the fint models above
    implementation(libs.validation.api)
    implementation(libs.kotlin.logging)
    implementation(libs.logstash.logback.encoder)

    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-restclient-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testImplementation(libs.mockito.kotlin)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

ktlint {
    version = libs.versions.ktlint.cli
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

tasks.test {
    useJUnitPlatform()
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.named("check") {
    dependsOn("ktlintCheck")
    dependsOn("jacocoTestReport")
}
