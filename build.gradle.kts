import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.9.24"
  application
  id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.valr.exchange"
version = "1.0.0-SNAPSHOT"

repositories {
  mavenCentral()
}

val vertxVersion = "4.5.8"
val junitJupiterVersion = "5.9.1"

val mainVerticleName = "com.valr.exchange.MainVerticle"
val launcherClassName = "io.vertx.core.Launcher"

val watchForChange = "src/**/*"
val doOnChange = "${projectDir}/gradlew classes"

application {
  mainClass.set(launcherClassName)
}

dependencies {
  implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
  implementation("io.vertx:vertx-web-client")
  implementation("io.vertx:vertx-web-validation")
  implementation("io.vertx:vertx-auth-jwt")
  implementation("io.vertx:vertx-web")
  implementation("io.vertx:vertx-lang-kotlin-coroutines")
  implementation("io.vertx:vertx-lang-kotlin")
  implementation(kotlin("stdlib-jdk8"))
  testImplementation("io.vertx:vertx-unit")
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
  testImplementation("io.mockk:mockk:1.13.11")
  testImplementation("io.vertx:vertx-junit5:4.5.8")
  runtimeOnly("io.netty:netty-resolver-dns-native-macos:4.1.111.Final:osx-x86_64")
  implementation("com.fasterxml.jackson.core:jackson-databind:2.17.1")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.1")
  implementation("io.vertx:vertx-config")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = "17"

tasks.withType<ShadowJar> {
  archiveClassifier.set("fat")
  manifest {
    attributes(mapOf("Main-Verticle" to mainVerticleName))
  }
  mergeServiceFiles()
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    events = setOf(PASSED, SKIPPED, FAILED)
  }
}

tasks.withType<JavaExec> {
  args = listOf(
    "run",
    mainVerticleName,
    "--redeploy=$watchForChange",
    "--launcher-class=$launcherClassName",
    "--on-redeploy=$doOnChange"
  )
}
