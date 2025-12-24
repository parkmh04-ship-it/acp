plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.jooq)
}

dependencies {
    implementation(project(":acp-shared"))
    
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.spring.boot.starter.webflux) // For calling PSP

    // DB (Merchant DB)
    implementation(libs.spring.boot.starter.jooq)
    implementation(libs.postgresql)
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    jooqGenerator(libs.postgresql)

    testImplementation(libs.spring.boot.starter.test)
}

// jOOQ Config for Merchant (To be configured later)
