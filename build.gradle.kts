plugins {
    alias(libs.plugins.changelog)
    alias(libs.plugins.dokka)
    `java-gradle-plugin`
    `kotlin-dsl`
    `maven-publish`
    signing
}

group = "ru.mipt.npm"
version = "0.1.0-kotlin-${libs.versions.kotlin}"

description = "A collection of starter plugins for kotlin development"

changelog.version.set(project.version.toString())

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://repo.kotlin.link")
}

java.targetCompatibility = JavaVersion.VERSION_11

kotlin.explicitApiWarning()

dependencies {
    api(libs.kotlin.gradle)
    implementation(libs.atomicfu.gradle)
    implementation(libs.changelog.gradle)
    implementation(libs.dokka.gradle)
    implementation(libs.kotlin.jupyter.gradle)
    implementation(libs.kotlin.serialization)

//    // nexus publishing plugin
//    implementation("io.github.gradle-nexus:publish-plugin:1.1.0")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

//declaring exported plugins

//gradlePlugin {
//    plugins {
//        create("common") {
//            id = "ru.mipt.npm.gradle.common"
//            description = "The generalized kscience plugin that works in conjunction with any kotlin plugin"
//            implementationClass = "ru.mipt.npm.gradle.KScienceCommonPlugin"
//        }
//    }
//}

//publishing the artifact

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.named("main").get().allSource)
}

val javadocsJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    archiveClassifier.set("javadoc")
    from(tasks.dokkaHtml)
}

afterEvaluate {
    publishing {
        val vcs = "https://github.com/mipt-npm/gradle-kotlin-start"

        // Process each publication we have in this project
        publications {

            withType<MavenPublication> {
                artifact(sourcesJar)
                artifact(javadocsJar)

                pom {
                    description.set(project.description)
                    url.set(vcs)

                    licenses {
                        license {
                            name.set("The Apache Software License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                            distribution.set("repo")
                        }
                    }

                    developers {
                        developer {
                            id.set("MIPT-NPM")
                            name.set("MIPT nuclear physics methods laboratory")
                            organization.set("MIPT")
                            organizationUrl.set("https://npm.mipt.ru")
                        }
                    }

                    scm {
                        url.set(vcs)
                        tag.set(project.version.toString())
                    }
                }
            }
        }

        val spaceRepo = "https://maven.pkg.jetbrains.space/mipt-npm/p/mipt-npm/maven"
        val spaceUser: String? = project.findProperty("publishing.space.user") as? String
        val spaceToken: String? = project.findProperty("publishing.space.token") as? String

        if (spaceUser != null && spaceToken != null) {
            project.logger.info("Adding mipt-npm Space publishing to project [${project.name}]")

            repositories.maven {
                name = "space"
                url = uri(spaceRepo)

                credentials {
                    username = spaceUser
                    password = spaceToken
                }
            }
        }

        val sonatypeUser: String? = project.findProperty("publishing.sonatype.user") as? String
        val sonatypePassword: String? = project.findProperty("publishing.sonatype.password") as? String

        if (sonatypeUser != null && sonatypePassword != null) {
            val sonatypeRepo: String = if (project.version.toString().contains("dev")) {
                "https://oss.sonatype.org/content/repositories/snapshots"
            } else {
                "https://oss.sonatype.org/service/local/staging/deploy/maven2"
            }

            repositories.maven {
                name = "sonatype"
                url = uri(sonatypeRepo)

                credentials {
                    username = sonatypeUser
                    password = sonatypePassword
                }
            }

            if (plugins.findPlugin("signing") == null) {
                apply<SigningPlugin>()
            }

            signing {
                //useGpgCmd()
                sign(publications)
            }
        }
    }
}