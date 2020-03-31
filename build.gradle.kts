plugins {
    java
    kotlin("jvm") version V.KOTLIN
    id("signing")
    id("maven-publish")
}

allprojects {
    repositories {
        google()
        jcenter()
    }

    apply {
        plugin("org.jetbrains.kotlin.jvm")
    }

    dependencies {
        implementation(kotlin("stdlib-jdk8"))
    }

    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_1_8
    }

    version = C.PROJECT_VERSION
    group = C.PROJECT_GROUP_ID

    description = C.PROJECT_DESCRIPTION

    tasks {
        compileKotlin {
            kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
        }
        compileTestKotlin {
            kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
        }
    }
}

subprojects {
    apply {
        plugin("maven-publish")
    }
    publishing {
        publications {
            create<MavenPublication>("maven") {
                val binaryJar = components["java"]

                val sourcesJar by tasks.creating(Jar::class) {
                    archiveClassifier.set("sources")
                    from(sourceSets["main"].allSource)
                }

                val javadocJar: Jar by tasks.creating(Jar::class) {
                    archiveClassifier.set("javadoc")
                    from("$buildDir/javadoc")
                }

                from(binaryJar)
                artifact(sourcesJar)
                artifact(javadocJar)
                pom(BuildConfig.pomAction)
            }
        }
    }
}