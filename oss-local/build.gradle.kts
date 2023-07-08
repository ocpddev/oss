dependencies {
    implementation(libs.slf4k)
    api(project(":oss-common"))

    api("org.springframework.boot:spring-boot-starter")

    testImplementation(project(":oss-test"))
}
