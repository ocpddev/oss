import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion
import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    java
    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.spring")
    id("org.jetbrains.dokka")
    id("io.spring.dependency-management")
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=all")
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports { mavenBom(SpringBootPlugin.BOM_COORDINATES) }
}

extra["kotlin.version"] = getKotlinPluginVersion()

dependencies {
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.springframework.boot:spring-boot-autoconfigure-processor")
    kapt("org.springframework.boot:spring-boot-configuration-processor")
    kapt("org.springframework.boot:spring-boot-autoconfigure-processor")
}
