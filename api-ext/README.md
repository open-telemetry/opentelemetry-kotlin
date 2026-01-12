# api-ext

This module contains extensions of the main public API of the SDK. These are not prescribed by the
OTel specification and are syntactic sugar that attempts to make the SDK easier to use for end-users.

## Guidelines for APIs

Please see the [contributing doc](../CONTRIBUTING.md) for design guidance on APIs. In short,
use interfaces to obscure concrete types & avoid default implementations as these tend to blend
separate concerns.
