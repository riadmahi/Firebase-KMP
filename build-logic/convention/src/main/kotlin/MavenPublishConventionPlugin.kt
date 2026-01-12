import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost

class MavenPublishConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.vanniktech.maven.publish")
            }

            extensions.configure<MavenPublishBaseExtension> {
                publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
                signAllPublications()

                coordinates(
                    groupId = "com.riadmahi.firebase",
                    artifactId = project.name,
                    version = project.version.toString()
                )

                pom {
                    name.set(project.name)
                    description.set("Firebase SDK for Kotlin Multiplatform")
                    inceptionYear.set("2025")
                    url.set("https://github.com/riadmahi/kmp-firebase")

                    licenses {
                        license {
                            name.set("Apache License 2.0")
                            url.set("https://opensource.org/licenses/Apache-2.0")
                        }
                    }

                    developers {
                        developer {
                            id.set("riadmahi")
                            name.set("Riad Mahi")
                        }
                    }

                    scm {
                        url.set("https://github.com/riadmahi/kmp-firebase")
                        connection.set("scm:git:git://github.com/riadmahi/kmp-firebase.git")
                        developerConnection.set("scm:git:ssh://git@github.com/riadmahi/kmp-firebase.git")
                    }
                }
            }
        }
    }
}
