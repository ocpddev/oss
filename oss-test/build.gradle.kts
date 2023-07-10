plugins {
    id("conventions.module")
}

dependencies {
    api(project(":oss-common"))
    api("org.springframework.boot:spring-boot-starter-test")
}
