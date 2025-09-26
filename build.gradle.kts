plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
}

group = "com.example"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

dependencies {
    // Ktor Server Core and Engine
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)

    // WebSocket Support
    implementation(libs.ktor.server.websockets)

    // Content Negotiation for handling JSON
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    // Logging
    implementation(libs.logback.classic)

    // Configuration from application.yaml
    implementation(libs.ktor.server.config.yaml)

    // Testing
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
}
