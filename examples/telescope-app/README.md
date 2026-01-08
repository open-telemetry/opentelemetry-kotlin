# Telescope App Example

The Telescope App is an Android example application that demonstrates the usage of the OpenTelemetry Kotlin API for instrumenting an Android application. 
It serves as a practical example of how to integrate OpenTelemetry into an Android app to collect and export telemetry data.
It follows the common path of integration of opentelemetry-java, but replacing the Java API with the new Kotlin API.

The app uses Jetpack Compose for the UI and AndroidX Navigation Compose for navigation and shows how to:
  - Set up OpenTelemetry SDK.
  - Create and manage spans.
  - Export telemetry data to a Grpc exporter.
  - Integrate with Android lifecycle.

## Project Structure

The Telescope App simulates a simple e-commerce application for telescopes. The app consists of two main screens:
- A telescope list screen that displays various telescopes available for purchase.
- A checkout screen that shows the purchase is complete.

The app uses OpenTelemetry to instrument screen navigation, and some general device resources.