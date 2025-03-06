import com.google.protobuf.gradle.id

plugins {
    jacoco
    id("org.springframework.boot") version "2.7.0"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("com.google.protobuf") version "0.9.4"
    kotlin("jvm") version "1.8.10"
    kotlin("plugin.spring") version "1.6.21"
    kotlin("plugin.jpa") version "1.6.21"
}

group = "TestSys"
version = "2.4.5-snapshot"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

sourceSets {
    main {
        proto {
            srcDir("libs/trik-testsys/protos")
        }
    }
}

val protobufVersion = "4.27.3"
val grpcVersion = "1.66.0"
val grpcktVersion = "1.4.1"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC.2")
    implementation("com.google.protobuf:protobuf-kotlin:${protobufVersion}")
    implementation("io.grpc:grpc-okhttp:${grpcVersion}")
    api("io.grpc:grpc-protobuf:${grpcVersion}")
    api("com.google.protobuf:protobuf-java-util:${protobufVersion}")
    api("com.google.protobuf:protobuf-kotlin:${protobufVersion}")
    api("io.grpc:grpc-kotlin-stub:${grpcktVersion}")
    api("io.grpc:grpc-stub:${grpcVersion}")

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-devtools")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

    implementation("org.thymeleaf.extras:thymeleaf-extras-java8time")

    implementation("org.springframework.session:spring-session-jdbc")

    implementation("org.zeroturnaround:zt-zip:1.17")
    implementation("org.yaml:snakeyaml:1.33")
    implementation("com.github.ua-parser:uap-java:1.5.4")

    implementation("io.springfox:springfox-swagger2:3.0.0")
    implementation("io.springfox:springfox-swagger-ui:3.0.0")
    implementation("io.springfox:springfox-boot-starter:3.0.0")

//    implementation("org.springframework.boot:spring-boot-starter-actuator")
//    implementation("io.micrometer:micrometer-core:1.6.6")
//    implementation("io.micrometer:micrometer-registry-prometheus:1.6.6")

    runtimeOnly("mysql:mysql-connector-java")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("com.h2database:h2")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
    testImplementation("org.mockito:mockito-core:2.1.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    testImplementation(platform("org.junit:junit-bom:5.9.0"))
    testImplementation(kotlin("test"))
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${protobufVersion}"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${grpcVersion}"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:${grpcktVersion}:jdk8@jar"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc")
                id("grpckt")
            }
            it.builtins {
                id("kotlin")
            }
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

val jacocoExclude = listOf(
    "**/configuration/**",
    "**/entities/**",
    "**/enums/**",
    "**/repositories/**",
    "**/services/**",
    "**/*Application*"
)

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
    useJUnitPlatform()
    maxHeapSize = "2G"
    extensions.configure<JacocoTaskExtension> {
        excludes = jacocoExclude
    }
    // Uncomment to run concurrent tests on your own PC
    /*jvmArgs(
        "-Xmx4096m",
        "--add-opens", "java.base/jdk.internal.misc=ALL-UNNAMED",
        "--add-exports", "java.base/jdk.internal.util=ALL-UNNAMED"
    )*/
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(false)
        csv.required.set(true)
        csv.outputLocation.set(file("${buildDir}/jacoco/report.csv"))
        html.outputLocation.set(file("${buildDir}/reports/jacoco"))
    }
    classDirectories.setFrom(classDirectories.files.map {
        fileTree(it).matching {
            exclude(jacocoExclude)
        }
    })
}