import java.net.URI

plugins {
    java
    application
}

val referenceCommit = "7a8e98c9affe9b032eeb7bd88b85e9379e6e6adc"
val archiveRoot = "InsanityStripper-$referenceCommit"
val archiveFile = layout.buildDirectory.file("reference-client.zip")
val extractionRoot = layout.buildDirectory.dir("reference-client")
val referenceClientDir = layout.buildDirectory.dir("reference-client/$archiveRoot/Client")

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

sourceSets {
    main {
        java.setSrcDirs(listOf(
            referenceClientDir.map { it.dir("src") },
            referenceClientDir.map { it.dir("sign") }
        ))
    }
}

application {
    mainClass.set("Jframe")
}

val prepareReferenceClient by tasks.registering {
    inputs.property("referenceCommit", referenceCommit)
    outputs.dir(referenceClientDir)

    doLast {
        val archive = archiveFile.get().asFile
        if (!archive.exists()) {
            archive.parentFile.mkdirs()
            URI("https://github.com/Johto-Foundry/InsanityStripper/archive/$referenceCommit.zip")
                .toURL()
                .openStream()
                .use { input -> archive.outputStream().use(input::copyTo) }
        }

        val extraction = extractionRoot.get().asFile
        delete(extraction)
        copy {
            from(zipTree(archive))
            into(extraction)
            include("$archiveRoot/Client/**")
        }

        val playerFile = referenceClientDir.get().file("src/Player.java").asFile
        val original = "\t\theadIcon = stream.readUnsignedByte();\n\t\tskullIcon = stream.readUnsignedByte();"
        val canonical = "\t\theadIcon = stream.readUnsignedByte();\n\t\tskullIcon = 255; // 317 appearance blocks do not contain a separate skull byte"
        val source = playerFile.readText()
        if (!source.contains(original)) {
            throw GradleException("Reference client appearance decoder no longer matches the pinned source.")
        }
        playerFile.writeText(source.replaceFirst(original, canonical))
    }
}

tasks.named<JavaCompile>("compileJava") {
    dependsOn(prepareReferenceClient)
    options.compilerArgs.addAll(listOf("-Xlint:deprecation", "-Xlint:unchecked"))
}

tasks.named<JavaExec>("run") {
    dependsOn(prepareReferenceClient)
    workingDir = referenceClientDir.get().asFile
    maxHeapSize = "1024m"
    args("10", "0", "highmem", "members", "32")
}
