# The OTel autoconfigure SPI is compileOnly in opentelemetry-java and is never present on Android.
-dontwarn io.opentelemetry.sdk.autoconfigure.spi.**

# OpenTelemetry Java uses these annotations at build time to generate value classes.
-dontwarn com.google.auto.value.AutoValue
-dontwarn com.google.auto.value.AutoValue$CopyAnnotations
-dontwarn io.opentelemetry.api.incubator.**
