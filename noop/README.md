# noop

This module provides a noop implementation of `api`. Wherever possible it
avoids object instantiation and unnecessary work.

Metric instruments are shared instances for each instrument type. They do not retain the supplied
name, unit, description or callbacks: `name` is always an empty string, and `unit` and `description`
are always `null`. Recording measurements and closing instruments or callback registrations have
no effect. Observable instruments return stable no-op measurement handles.
