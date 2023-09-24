plugins {
    id("conventions.module")
    id("conventions.publishing")
    `java-test-fixtures`
}

dependencies {
    api("org.springframework.boot:spring-boot-autoconfigure")
    testFixturesApi("org.springframework.boot:spring-boot-starter-test")
}
