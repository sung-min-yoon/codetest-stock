plugins {
    java
    id("org.springframework.boot") version "3.3.2"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "com.codingtest"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-logging")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    implementation("org.redisson:redisson-spring-boot-starter:3.33.0")
    implementation("org.projectlombok:lombok:1.18.34")
    runtimeOnly("com.mysql:mysql-connector-j:8.3.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:testcontainers:1.20.1")
    testImplementation("org.testcontainers:junit-jupiter:1.20.1")
    testImplementation("org.testcontainers:mysql:1.20.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.3")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
