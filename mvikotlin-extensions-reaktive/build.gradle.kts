plugins {
    id("kotlin-multiplatform")
    id("com.android.library")
    id("com.arkivanov.gradle.setup")
}

setupMultiplatform {
    targets()
    publications()
    binaryCompatibilityValidator()
}

kotlin {
    sourceSets {
        named("commonMain") {
            dependencies {
                implementation(project(":mvikotlin"))
                implementation(project(":rx"))
                implementation(project(":rx-internal"))
                implementation(project(":utils-internal"))
                implementation(deps.reaktive.reaktive)
                implementation(deps.reaktive.reaktiveAnnotations)
            }
        }
    }
}
