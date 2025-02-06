import arc.files.Fi
import arc.util.OS
import arc.util.serialization.Jval
import de.undercouch.gradle.tasks.download.Download
import ent.EntityAnnoExtension
import java.io.BufferedWriter
import ent.*
import mmc.JarMindustryTask
import java.io.*
import org.jetbrains.kotlin.gradle.plugin.KaptExtension

buildscript {
    val arcVersion: String by project
    val useJitpack = property("mindustryBE").toString().toBooleanStrict()

    dependencies {
        classpath("com.github.Anuken.Arc:arc-core:$arcVersion")
    }

    repositories {
        if (!useJitpack) maven("https://raw.githubusercontent.com/Zelaux/MindustryRepo/master/repository")
        maven("https://jitpack.io")
    }
}

plugins {
    java
    id("de.undercouch.download") version "5.4.0"
    id("com.github.GlennFolker.EntityAnno") apply false
}
val asmLib: (String) -> Any = {
    val asmLibVersion: String by project
    val name = it.trim(':').replace(':', '-')
    try {
        project(":JavaAsmLib:$it")
    } catch (e: Exception) {
        "com.github.Zelaux.JavaAsmExtension:$name:$asmLibVersion"
    }
}

val arcVersion: String by project
val arcLibraryVersion: String by project
val zelauxCoreVersion: String by project
val mindustryVersion: String by project
val mindustryBEVersion: String by project
val entVersion: String by project

val modName: String by project
val modArtifact: String by project
val modFetch: String by project
val modGenSrc: String by project
val modGen: String by project

val androidSdkVersion: String by project
val androidBuildVersion: String by project
val androidMinVersion: String by project

val useJitpack = property("mindustryBE").toString().toBooleanStrict()

fun arc(module: String): String {
    return "com.github.Anuken.Arc$module:$arcVersion"
}

fun arcLibrary(module: String): String {
    return "com.github.Zelaux.ArcLibrary$module:$arcLibraryVersion"
}

fun zelauxCore(module: String): String {
    return "com.github.Zelaux.MindustryModCore:${module.trim(':').replace(':', '-')}:$zelauxCoreVersion"
}

fun mindustry(module: String): String {
    return "com.github.Anuken.Mindustry$module:$mindustryVersion"
}

fun entity(module: String): String {
    return "com.github.GlennFolker.EntityAnno$module:$entVersion"
}

extra.set("asmLib", asmLib)
project(":") {
    apply(plugin = "java")
    sourceSets["main"].java.setSrcDirs(listOf(layout.projectDirectory.dir("src")))

    configurations.configureEach {
        // Resolve the correct Mindustry dependency, and force Arc version.
        resolutionStrategy.eachDependency {
            if (useJitpack && requested.group == "com.github.Anuken.Mindustry") {
                useTarget("com.github.Anuken.MindustryJitpack:${requested.module.name}:$mindustryBEVersion")
            } else if (requested.group == "com.github.Anuken.Arc") {
                useVersion(arcVersion)
            }
        }
    }

    dependencies {
        // Downgrade Java 9+ syntax into being available in Java 8.
        //moved into :annotation because of 'missing opens issue'
//        annotationProcessor(entity(":downgrader"))
    }

    repositories {
        // Necessary Maven repositories to pull dependencies from.
        mavenCentral()
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://oss.sonatype.org/content/repositories/releases/")
        maven("https://raw.githubusercontent.com/GlennFolker/EntityAnnoMaven/main")

        // Use Zelaux's non-buggy repository for release Mindustry and Arc builds.
        if (!useJitpack) maven("https://raw.githubusercontent.com/Zelaux/MindustryRepo/master/repository")
        maven("https://raw.githubusercontent.com/Zelaux/Repo/master/repository")//for ArcLibrary
        maven("https://jitpack.io")
    }

    tasks.withType<JavaCompile>().configureEach {
        // Use Java 17+ syntax, but target Java 8 bytecode version.
        sourceCompatibility = "17"
        options.apply {
            release = 8
            compilerArgs.add("-Xlint:-options")

            isIncremental = true
            encoding = "UTF-8"
        }
    }
}

project(":") {
    //sometimes task checkKotlinGradlePluginConfigurationErrors are missing...
//    tasks.register("checkKotlinGradlePluginConfigurationErrors1"){    }
    tasks.register("mindustryJar", JarMindustryTask::class) {
        dependsOn(tasks.getByPath("jar"))
        group = "build"
    }

    apply(plugin = "com.github.GlennFolker.EntityAnno")
    configure<EntityAnnoExtension> {
        modName = project.properties["modName"].toString()
        mindustryVersion = project.properties[if (useJitpack) "mindustryBEVersion" else "mindustryVersion"].toString()
        isJitpack = useJitpack
        revisionDir = layout.projectDirectory.dir("revisions").asFile
        fetchPackage = modFetch
        genSrcPackage = modGenSrc
        genPackage = modGen
    }
    configure<KaptExtension> {
        arguments {
            arg("ROOT_DIRECTORY", project.rootDir.canonicalPath)
            arg("rootPackage", "ol")
            arg("classPrefix", "Ol")
        }
    }
    dependencies {

    //Added debuging diring compilation to debug annotation processors
    tasks.withType(JavaCompile::class).configureEach {
        options.isDebug = true
        options.isFork = true
        options.compilerArgs.add("-g")

        options.forkOptions.jvmArgs!!.add(
            "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5008"
        )
    }
    dependencies {
        annotationProcessor("org.projectlombok:lombok:1.18.32")
        annotationProcessor(asmLib("annotations:debug-print"))
        annotationProcessor(project(":annotations"))

        // Use the entity generation annotation processor.
        var kaptAnno = listOf(
            entity(":entity"),
            zelauxCore(":annotations:remote")
        )
        kaptAnno.forEach {
            compileOnly(it)
            add("kapt", it)
        }

        compileOnly("org.jetbrains:annotations:24.0.1")

        compileOnly(mindustry(":core"))
        compileOnly(arc(":arc-core"))
        implementation(arcLibrary(":graphics-draw3d"))
        implementation(arcLibrary(":graphics-dashDraw"))
        implementation(arcLibrary(":graphics-extendedDraw"))
    }

    val jar = tasks.named<Jar>("jar") {
        archiveFileName = "${modArtifact}Desktop.jar"

        val meta = layout.projectDirectory.file("$temporaryDir/mod.json")
        from(
            files(sourceSets["main"].output.classesDirs),
            files(sourceSets["main"].output.resourcesDir),
            configurations.runtimeClasspath.map { conf -> conf.map { if (it.isDirectory) it else zipTree(it) } },

            files(layout.projectDirectory.dir("assets")),
            layout.projectDirectory.file("icon.png"),
            meta
        )

        metaInf.from(layout.projectDirectory.file("LICENSE"))
        doFirst {
            // Deliberately check if the mod meta is actually written in HJSON, since, well, some people actually use
            // it. But this is also not mentioned in the `README.md`, for the mischievous reason of driving beginners
            // into using JSON instead.
            val metaJson = layout.projectDirectory.file("mod.json")
            val metaHjson = layout.projectDirectory.file("mod.hjson")

            if (metaJson.asFile.exists() && metaHjson.asFile.exists()) {
                throw IllegalStateException("Ambiguous mod meta: both `mod.json` and `mod.hjson` exist.")
            } else if (!metaJson.asFile.exists() && !metaHjson.asFile.exists()) {
                throw IllegalStateException("Missing mod meta: neither `mod.json` nor `mod.hjson` exist.")
            }

            val isJson = metaJson.asFile.exists()
            val map = (if (isJson) metaJson else metaHjson).asFile
                .reader(Charsets.UTF_8)
                .use { Jval.read(it) }

            map.put("name", modName)
            meta.asFile.writer(Charsets.UTF_8).use { file -> BufferedWriter(file).use { map.writeTo(it, Jval.Jformat.formatted) } }
        }
    }

    tasks.register<Jar>("dex") {
        inputs.files(jar)
        group = "android"
        archiveFileName = "$modArtifact.jar"

        val desktopJar = jar.flatMap { it.archiveFile }
        val dexJar = File(temporaryDir, "Dex.jar")

        from(zipTree(desktopJar), zipTree(dexJar))
        doFirst {
            logger.lifecycle("Running `d8`.")
            providers.exec {
                // Find Android SDK root.
                val sdkRoot = File(
                    OS.env("ANDROID_SDK_ROOT") ?: OS.env("ANDROID_HOME") ?: throw IllegalStateException("Neither `ANDROID_SDK_ROOT` nor `ANDROID_HOME` is set.")
                )

                // Find `d8`.
                val d8 = File(sdkRoot, "build-tools/$androidBuildVersion/${if (OS.isWindows) "d8.bat" else "d8"}")
                if (!d8.exists()) throw IllegalStateException("Android SDK `build-tools;$androidBuildVersion` isn't installed or is corrupted")

                // Initialize a release build.
                val input = desktopJar.get().asFile
                val command = arrayListOf("$d8", "--release", "--min-api", androidMinVersion, "--output", "$dexJar", "$input")

                // Include all compile and runtime classpath.
                (configurations.compileClasspath.get().toList() + configurations.runtimeClasspath.get().toList()).forEach {
                    if (it.exists()) command.addAll(arrayOf("--classpath", it.path))
                }

                // Include Android platform as library.
                val androidJar = File(sdkRoot, "platforms/android-$androidSdkVersion/android.jar")
                if (!androidJar.exists()) throw IllegalStateException("Android SDK `platforms;android-$androidSdkVersion` isn't installed or is corrupted")

                command.addAll(arrayOf("--lib", "$androidJar"))
                if (OS.isWindows) command.addAll(0, arrayOf("cmd", "/c").toList())

                // Run `d8`.
                commandLine(command)
            }.result.get().rethrowFailure()
        }
    }

    tasks.register<Download>("fetchClient") {
        group = "run"
        src("https://github.com/Anuken/Mindustry/releases/download/$mindustryVersion/Mindustry.jar")
        dest(file("$rootDir/run/Mindustry.jar"))
        overwrite(false)
    }

    tasks.register<JavaExec>("runClient") {
        group = "run"
        dependsOn("fetchClient")
        dependsOn("jar")

        val modFilename = "${project.name}Desktop.jar"
        doFirst {
            copy {
                from("$rootDir/build/libs/$modFilename")
                into("$rootDir/run/mods")
                rename { modFilename }
            }
        }

        environment("MINDUSTRY_DATA_DIR", "$rootDir/run")
        classpath(files("$rootDir/run/Mindustry.jar"))
        mainClass.set("mindustry.desktop.DesktopLauncher")
    }

    tasks.register<DefaultTask>("install") {
        dependsOn("jar")
        doLast {
            val folder = Fi.get(OS.getAppDataDirectoryString("Mindustry")).child("mods")
            folder.mkdirs()

            val input = Fi.get("$rootDir/build/libs/${project.name}Desktop.jar")
            folder.child(input.name()).delete()
            input.copyTo(folder)
            logger.lifecycle("Copied :jar output to $folder.")
        }
    }
}