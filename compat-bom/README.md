# compat-bom

This module contains a Bill Of Materials (BOM) for the `compat` module so that
the `opentelemetry-java` and `opentelemetry-kotlin` are constrained to use a specific, supported version.

It imports the `bom` module (which aligns all `opentelemetry-kotlin` artifacts) plus the stable and alpha
`opentelemetry-java` BOMs that `compat` is built against.
