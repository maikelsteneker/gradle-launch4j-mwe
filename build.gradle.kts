import edu.sc.seis.launch4j.tasks.DefaultLaunch4jTask

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.4.32"
    application
    id("edu.sc.seis.launch4j") version "2.5.0"
}

repositories {
    jcenter()
}

dependencies {
}

application {
    mainClass.set("com.tiobe.tics.wrapper.AppKt")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = application.mainClass
    }
    configurations["compileClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile))
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.register<DefaultTask>("executable") {
    description = "Creates self-executable file"
    group = "Distribution"

    inputs.files(tasks.withType<Jar>())
    outputs.file("$buildDir/TICS")

    doLast {
        val execFile = outputs.files.singleFile
        execFile.writeBytes(
            """#!/bin/sh
              |D=${'$'}(dirname "${'$'}0")
              |J=${'$'}D/../jre/bin/java
              |exec ${'$'}J -Xmx512m -jar "${'$'}0" "${'$'}@"
              |
              |""".trimMargin().toByteArray()
        )
        execFile.appendBytes(inputs.files.singleFile.readBytes())
        execFile.setExecutable(true, false)
    }
}

tasks.withType<DefaultLaunch4jTask> {
    bundledJre64Bit = true
    bundledJrePath = "../jre"
    dontWrapJar = true
    headerType = "console"
    jar = "TICS"
    jvmOptions = setOf("""-Dtics.wrapper.self="%EXEFILE%"""")
    outfile = "TICS.exe"
    chdir = ""
}
