// region getting-started-install
dependencies {
    implementation(platform("io.opentelemetry.kotlin:bom:{{otelKotlinVersion}}"))
    implementation("io.opentelemetry.kotlin:implementation")
    implementation("io.opentelemetry.kotlin:exporters-otlp")
}
// end-region
