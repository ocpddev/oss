plugins {
    id("conventions.module")
    id("conventions.publishing")
}

dependencies {
    implementation(libs.slf4k)

    api(project(":oss-common"))
    implementation(libs.aws.sdk.s3)

    api("org.springframework.boot:spring-boot-starter")

    testImplementation(project(":oss-test"))
    testImplementation(libs.localstack.utils)
}
