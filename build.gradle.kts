import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    alias(libs.plugins.jvm)
    alias(libs.plugins.kapt)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management)
    `maven-publish`
    signing
}

group = "dev.ocpd.oss"

repositories {
    mavenCentral()
}

subprojects {

    val plugins = rootProject.project.libs.plugins

    apply {
        plugin(plugins.jvm.get().pluginId)
        plugin(plugins.kapt.get().pluginId)
        plugin(plugins.dokka.get().pluginId)
        plugin(plugins.kotlin.spring.get().pluginId)
        plugin(plugins.spring.boot.get().pluginId)
        plugin(plugins.spring.dependency.management.get().pluginId)
        plugin("maven-publish")
        plugin<MavenPublishPlugin>()
        plugin<SigningPlugin>()
    }

    group = rootProject.group
    version = rootProject.version

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        withSourcesJar()
        withJavadocJar()
    }

    tasks.named<BootJar>("bootJar") {
        enabled = false
    }

    tasks.named<Jar>("javadocJar") {
        from(tasks.named("dokkaJavadoc"))
        duplicatesStrategy = DuplicatesStrategy.WARN
    }

    tasks.named("dokkaJavadoc") {
        dependsOn("kaptKotlin")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=all")
            jvmTarget = JavaVersion.VERSION_17.toString()
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    repositories {
        mavenCentral()
    }

    dependencyManagement {
        imports { mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES) }
    }

    extra["kotlin.version"] = getKotlinPluginVersion()

    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
                pom {
                    name.set("oss")
                    description.set("OSS support")
                    url.set("https://github.com/ocpddev/oss")
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    scm {
                        url.set("https://github.com/ocpddev/oss")
                    }
                    developers {
                        developer {
                            id.set("sola")
                            name.set("Sola")
                            email.set("sola@ocpd.dev")
                        }
                    }
                }
            }
        }
        repositories {
            maven {
                val releaseUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                val snapshotUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")

                url = if (version.toString().endsWith("-SNAPSHOT")) snapshotUrl else releaseUrl

                credentials {
                    username = project.findSecret("ossrh.username", "OSSRH_USERNAME")
                    password = project.findSecret("ossrh.password", "OSSRH_PASSWORD")
                }
            }
        }
    }

    signing {
        val key = findSecret("ocpd.sign.key", "OCPD_SIGN_KEY")
        if (key != null) {
            val keyId = findSecret("ocpd.sign.key.id", "OCPD_SIGN_KEY_ID")
            val passphrase = findSecret("ocpd.sign.passphrase", "OCPD_SIGN_PASSPHRASE") ?: ""
            useInMemoryPgpKeys(keyId, key, passphrase)
        }
        sign(publishing.publications["maven"])

    }
}

fun Project.findSecret(key: String, env: String): String? =
    project.findProperty(key) as? String ?: System.getenv(env)
