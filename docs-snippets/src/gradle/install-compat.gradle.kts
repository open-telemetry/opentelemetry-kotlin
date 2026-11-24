// region getting-started-install-compat
dependencies {
    // compat-bom also aligns the opentelemetry-java artifacts that compat uses
    implementation(platform("io.opentelemetry.kotlin:compat-bom:{{otelKotlinVersion}}"))
    implementation("io.opentelemetry.kotlin:compat")
    implementation("io.opentelemetry.kotlin:exporters-otlp")
}
// end-region
