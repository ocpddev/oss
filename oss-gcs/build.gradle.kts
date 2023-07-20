plugins {
    id("conventions.module")
    id("conventions.publishing")
}

dependencies {
    implementation(libs.slf4k)

    api(project(":oss-common"))
    api("org.springframework.boot:spring-boot-starter")

    // Spring Cloud GCS
    implementation(libs.spring.cloud.gcs)

    testImplementation(project(":oss-test"))
    testImplementation(libs.testcontainers)
}
