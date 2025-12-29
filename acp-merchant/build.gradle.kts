buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.postgresql:postgresql:42.7.2")
        classpath("org.flywaydb:flyway-database-postgresql:10.10.0")
    }
}

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.jooq)
    alias(libs.plugins.flyway)
}

flyway {
    driver = "org.postgresql.Driver"
    url = "jdbc:postgresql://localhost:5432/acp"
    user = "user"
    password = "password"
    schemas = arrayOf("merchant")
    createSchemas = true
    baselineOnMigrate = true
}

dependencies {
    implementation(project(":acp-shared"))
    
    // Flyway for Gradle
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.spring.boot.starter.webflux) // For calling PSP
    implementation(libs.kotlin.logging) // Kotlin Logging

    // DB (Merchant DB)
    implementation(libs.spring.boot.starter.jooq)
    implementation(libs.postgresql)
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.kotlin.coroutines.reactor)
    jooqGenerator(libs.postgresql)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.kotlin.coroutines.test)
}

sourceSets {
    main {
        kotlin.srcDir("build/generated/jooq")
    }
}

jooq {
    version.set(libs.versions.jooq.get())

    configurations {
        create("main") {
            jooqConfiguration.apply {
                jdbc.apply {
                    driver = "org.postgresql.Driver"
                    url = "jdbc:postgresql://localhost:5432/acp"
                    user = "user"
                    password = "password"
                }
                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator"
                    database.apply {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        inputSchema = "merchant"
                    }
                    generate.apply {
                        isDeprecated = false
                        isRecords = true
                        isImmutablePojos = true
                        isFluentSetters = true
                    }
                    target.apply {
                        packageName = "com.acp.merchant.generated.jooq"
                        directory = "build/generated/jooq"
                    }
                }
            }
        }
    }
}
