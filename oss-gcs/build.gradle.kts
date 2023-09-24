plugins {
    id("conventions.module")
    id("conventions.publishing")
}

dependencies {
    implementation(libs.slf4k)

    api(project(":oss-common"))
    api("org.springframework.boot:spring-boot-starter")

    implementation(libs.spring.cloud.gcs)

    testImplementation(testFixtures(project(":oss-common")))
    testImplementation(libs.testcontainers)
}
