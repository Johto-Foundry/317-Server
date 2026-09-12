plugins {
    java
    application
}

group = "server"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("io.netty:netty-bom:4.2.18.Final"))
    implementation("io.netty:netty-transport")

    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

application {
    mainClass.set("server.Server")
}

tasks.test {
    useJUnitPlatform()
}
