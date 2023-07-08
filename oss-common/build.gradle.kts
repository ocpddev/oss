dependencies {
    implementation("org.springframework:spring-context")
    implementation("org.springframework.boot:spring-boot-autoconfigure")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.springframework.boot:spring-boot-autoconfigure-processor")
    kapt("org.springframework.boot:spring-boot-configuration-processor")
    kapt("org.springframework.boot:spring-boot-autoconfigure-processor")
}
