# bom

This module contains a Bill Of Materials (BOM) that constrains every published `opentelemetry-kotlin` artifact to the
same version. Importing the BOM only aligns versions: it does not add a dependency on any module.

```kotlin
dependencies {
    implementation(platform("io.opentelemetry.kotlin:bom:<version>"))
    implementation("io.opentelemetry.kotlin:implementation")
}
```

If you use the `compat` module, import `compat-bom` instead. It includes this BOM and also aligns the
`opentelemetry-java` artifacts that `compat` is built against.
