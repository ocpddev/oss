plugins {
    alias(libs.plugins.jvm)
    alias(libs.plugins.kapt)
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management)
}

dependencyManagement {
    imports { mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES) }
}


dependencies {
    implementation(libs.slf4k)
    api(project(":oss-common"))

    api("org.springframework.boot:spring-boot-starter")

    testImplementation(project(":oss-test"))
}
