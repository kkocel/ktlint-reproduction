import org.gradle.api.tasks.wrapper.Wrapper.DistributionType

val awsSdkVersion = "2.17.261"
val archunitVersion = "0.23.1"
val caffeineVersion = "3.1.1"
val commonsTextVersion = "1.9"
val embeddedRedisVersion = "0.7.3"
val handlebarsVersion = "4.3.0"
val jsoupVersion = "1.15.3"
val kboxVersion = "0.16.0"
val kotlinLoggingVersion = "2.1.23"
val kotlinTestVersion = "5.4.2"
val micrometerJvmExtrasVersion = "0.2.2"
val mp3SpiVersion = "1.9.5.4"
val resilience4jVersion = "1.7.1"
val schedLockVersion = "4.41.0"
val wireMockVersion = "2.27.2"
val xmlJacksonVersion = "2.13.3"

plugins {
    id("org.springframework.boot") version "2.7.3"
    id("io.spring.dependency-management") version "1.0.13.RELEASE"
    id("pl.allegro.tech.build.axion-release") version "1.14.0"
    id("com.github.ben-manes.versions") version "0.42.0"
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
    id("com.gorylenko.gradle-git-properties") version "2.4.1"
    id("io.gitlab.arturbosch.detekt") version "1.21.0"
    id("com.google.osdetector") version "1.7.0"
    kotlin("jvm") version "1.7.10"
    kotlin("plugin.spring") version "1.7.10"
}

repositories {
    maven(url = "https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
    mavenCentral()
}

kotlin {
    jvmToolchain {
        this.languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.getByName<Jar>("jar") {
    enabled = false
}

group = "foo.bar"
project.version = scmVersion.version

dependencies {

    implementation("org.springframework.boot:spring-boot-starter") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }

    if (osdetector.arch.equals("aarch_64")) {
        implementation("io.netty:netty-all")
    }

    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("io.projectreactor.addons:reactor-extra")

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    implementation("com.github.jknack:handlebars:$handlebarsVersion")
    implementation("com.github.jknack:handlebars-jackson2:$handlebarsVersion")

    implementation("org.apache.commons:commons-text:$commonsTextVersion")

    implementation(platform("software.amazon.awssdk:bom:$awsSdkVersion"))
    implementation("software.amazon.awssdk:s3")
    implementation("software.amazon.awssdk:netty-nio-client")

    implementation("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("io.github.mweirauch:micrometer-jvm-extras:$micrometerJvmExtrasVersion")
    implementation("io.github.resilience4j:resilience4j-reactor:$resilience4jVersion")
    implementation("io.github.resilience4j:resilience4j-ratelimiter:$resilience4jVersion")
    implementation("io.github.resilience4j:resilience4j-micrometer:$resilience4jVersion")

    implementation("net.javacrumbs.shedlock:shedlock-spring:$schedLockVersion")
    implementation("net.javacrumbs.shedlock:shedlock-provider-redis-spring:$schedLockVersion")
    implementation("net.javacrumbs.shedlock:shedlock-provider-inmemory:$schedLockVersion")

    implementation("com.github.ben-manes.caffeine:caffeine:$caffeineVersion")
    implementation("com.googlecode.soundlibs:mp3spi:$mp3SpiVersion")
    implementation("org.jsoup:jsoup:$jsoupVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.junit.platform:junit-platform-engine")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "junit")
        exclude(group = "org.junit.vintage")
    }
    testImplementation("io.kotest:kotest-runner-junit5:$kotlinTestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotlinTestVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("it.ozimov:embedded-redis:$embeddedRedisVersion") {
        exclude(group = "org.slf4j")
    }
    testImplementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:$xmlJacksonVersion")
    testImplementation("com.github.tomakehurst:wiremock:$wireMockVersion")
    testImplementation("com.tngtech.archunit:archunit-junit5:$archunitVersion")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.wrapper {
    gradleVersion = "7.5"
    distributionType = DistributionType.BIN
}

detekt {
    parallel = true
}

tasks.named<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask>("dependencyUpdates").configure {
    gradleReleaseChannel = "current"
    rejectVersionIf {
        isNonStable(candidate.version)
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}
