# config-dsl

This module contains the implementations of the programmatic configuration DSL, shared by
`implementation` and `compat`. The DSL interfaces themselves live in `sdk-api`.

Each implementation maps what the caller configured onto the `behavior` it supplies.
