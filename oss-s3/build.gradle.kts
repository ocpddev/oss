plugins {
    id("conventions.module")
    id("conventions.publishing")
}

dependencyManagement {
    imports {
        mavenBom(libs.aws.sdk.bom.get().toString())
    }
}

dependencies {
    implementation(libs.slf4k)

    api(project(":oss-common"))
    implementation("software.amazon.awssdk:s3")

    api("org.springframework.boot:spring-boot-starter")

    testImplementation(project(":oss-test"))
    testImplementation(libs.localstack.utils)
}
