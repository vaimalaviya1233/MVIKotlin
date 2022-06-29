import com.arkivanov.gradle.bundle
import com.arkivanov.gradle.setupMultiplatform
import com.arkivanov.gradle.setupSourceSets
import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id("kotlin-multiplatform")
    id("com.arkivanov.gradle.setup")
    id("org.jetbrains.compose")
}

setupMultiplatform {
    jvm {
        withJava()
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

kotlin {
    setupSourceSets {
        val jvm by bundle()


        jvm.main.dependencies {
            implementation(project(":mvikotlin"))
            implementation(project(":mvikotlin-main"))
            implementation(project(":mvikotlin-timetravel-client:client-internal"))
            implementation(project(":mvikotlin-timetravel-proto-internal"))
            implementation(deps.reaktive.reaktive)
            implementation(deps.reaktive.coroutinesInterop)
            implementation(deps.russhwolf.multiplatformSettings)
            implementation(compose.desktop.currentOs)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.materialIconsExtended)
        }
    }
}

configurations.all {
    resolutionStrategy.dependencySubstitution {
        substitute(module("org.jetbrains.compose.compiler:compiler")).apply {
            using(module("androidx.compose.compiler:compiler:1.2.0-dev-k1.7.0-53370d83bb1"))
//            using(module("org.jetbrains.compose.compiler:compiler:1.2.0-alpha01-dev725"))
        }

//        substitute(module("org.jetbrains.compose.compiler:compiler"))
//            .using(module("androidx.compose.compiler:compiler:1.2.0-dev-k1.7.0-53370d83bb1"))
////            .using(module("androidx.compose.compiler:compiler:1.2.0-rc02"))
//            .because("using the compose pre-release compiler")
    }
}

compose.desktop {
    application {
        mainClass = "com.arkivanov.mvikotlin.timetravel.client.desktop.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Rpm)
            packageName = "MVIKotlin Time Travel Client"
            packageVersion = deps.versions.timeTravelApp.get()

            windows {
                upgradeUuid = "B0B34196-90BE-4398-99BE-8E650EBECC78"
            }
        }
    }
}
