plugins {
    id("conventions.module")
    id("conventions.publishing")
}

dependencies {
    implementation(libs.slf4k)

    api(project(":oss-common"))
    api("org.springframework.boot:spring-boot-starter")

    implementation(libs.aws.sdk.s3)

    testImplementation(testFixtures(project(":oss-common")))
    testImplementation(libs.localstack.utils)
}
