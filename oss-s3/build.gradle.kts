dependencies {
    implementation(libs.slf4k)

    api(project(":oss-common"))
    implementation(platform(libs.aws.sdk.bom))
    implementation("software.amazon.awssdk:s3")

    api("org.springframework.boot:spring-boot-starter")

    testImplementation(project(":oss-test"))
    testImplementation(libs.localstack.utils)
}
