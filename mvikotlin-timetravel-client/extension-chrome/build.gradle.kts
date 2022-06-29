import org.jetbrains.kotlin.incremental.mkdirsOrThrow

plugins {
    id("org.jetbrains.kotlin.js")
    id("com.arkivanov.gradle.setup")
}

kotlin {
    js(IR) {
        browser()
        binaries.library()
    }
}

dependencies {
    implementation(project(":mvikotlin-main"))
    implementation(project(":mvikotlin-timetravel-proto-internal"))
    implementation(project(":mvikotlin-timetravel-client:client-internal"))
    implementation(deps.reaktive.reaktive)

    implementation("org.jetbrains.kotlin-wrappers:kotlin-react")
    implementation(npm("react", "*"))
    implementation("org.jetbrains.kotlin-wrappers:kotlin-emotion")
    implementation(npm("@emotion/css", "*"))
    implementation(npm("@emotion/react", "*"))
    implementation(npm("@emotion/styled", "*"))
    implementation("org.jetbrains.kotlin-wrappers:kotlin-mui")
    implementation(npm("@mui/material", "*"))
    implementation("org.jetbrains.kotlin-wrappers:kotlin-mui-icons")
    implementation(npm("@mui/icons-material", "*"))
    implementation(enforcedPlatform(deps.kotlinWrappers.kotlinWrappersBom))
}

val entryPointFileName = "MVIKotlin-extension-chrome.js"
val libraryDir = File(buildDir, "productionLibrary")
val distDir = File(buildDir, "dist")

tasks.register<Exec>(name = "browserLibraryNpmInstall") {
    dependsOn("browserProductionLibraryDistribution")
    group = "kotlin browser"
    description = "Installs npm dependencies for the browser library"
    workingDir = libraryDir
    commandLine = listOf("npm", "install")
}

tasks.register<Delete>(name = "browserLibraryCleanDist") {
    group = "kotlin browser"
    description = "Cleans the browser library dist folder"
    delete(distDir)

    doLast {
        distDir.mkdirsOrThrow()
    }
}

tasks.register<Exec>(name = "browserLibraryWebpack") {
    dependsOn("browserLibraryNpmInstall", "browserLibraryCleanDist")
    group = "kotlin browser"
    description = "Create a browser library bundle"
    workingDir = libraryDir

    commandLine =
        listOf(
            "npx",
            "webpack",
            "--target", "web",
            "--mode", "production",
            "--entry", "./$entryPointFileName",
            "--output-path", distDir.absolutePath,
            "--output-filename", entryPointFileName,
        )
}

tasks.register<Copy>(name = "browserLibraryCopyResources") {
    dependsOn("browserLibraryCleanDist")
    group = "kotlin browser"
    description = "Copy resources for the browser library"
    from(File(projectDir, "src/main/resources"))
    into(distDir)
}

tasks.register("browserLibraryBundle") {
    dependsOn("browserLibraryWebpack", "browserLibraryCopyResources")
    group = "kotlin browser"
    description = "Create a browser library bundled distribution"
}
