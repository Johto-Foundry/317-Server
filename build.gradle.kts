plugins {
    java
    application
}

group = "server"
version = "0.0.0.01"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

application {
    mainClass.set("server.Server")
}

tasks.test {
    useJUnitPlatform()
}
