plugins {
    alias(libs.plugins.jvm)
    alias(libs.plugins.kapt)
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management)
}

dependencyManagement {
    imports {
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        mavenBom(libs.spring.cloud.gcp.get().toString())
    }
}


dependencies {
    implementation(libs.slf4k)

    api(project(":oss-common"))
    api("org.springframework.boot:spring-boot-starter")

    // Spring Cloud GCP
    implementation("com.google.cloud:spring-cloud-gcp-starter-storage")

    testImplementation(project(":oss-test"))
    testImplementation(libs.testcontainers)
}
