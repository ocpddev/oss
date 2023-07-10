plugins {
    id("conventions.module")
    id("conventions.publishing")
}

dependencyManagement {
    imports {
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
