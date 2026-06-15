plugins {
    id("java")
    id("checkstyle")
    jacoco
    application
    checkstyle
    id("org.sonarqube") version "7.0.1.6134"
}

group = "hexlet.code"
version = "1.0-SNAPSHOT"

checkstyle {
    toolVersion = "10.12.4"
    configFile = file("config/checkstyle/checkstyle.xml")
    isIgnoreFailures = false
    maxWarnings = 0
}

application {
    mainClass.set("hexlet.code.App")
}

jacoco {
    toolVersion = "0.8.14"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("io.javalin:javalin-testtools:6.1.3")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    implementation("io.javalin:javalin:6.1.3")
    implementation("org.slf4j:slf4j-simple:2.0.12")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("com.h2database:h2:2.2.224")
    implementation("com.konghq:unirest-java:3.14.5")
    implementation("org.jsoup:jsoup:1.17.2")
    implementation("org.postgresql:postgresql:42.7.3")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("gg.jte:jte:3.1.9")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.1")
    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")
    testCompileOnly("org.projectlombok:lombok:1.18.42")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.42")
}

sonar {
   properties {
        property("sonar.projectKey", "SaintCap_java-project-72")
        property("sonar.organization", "saintcap")
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks {
    checkstyleMain {
        dependsOn(compileJava)
    }

    checkstyleTest {
        dependsOn(compileTestJava)
    }
}