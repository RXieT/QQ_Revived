# Miuix glass material source

- Upstream: https://github.com/compose-miuix-ui/miuix/pull/423
- Pinned upstream commit: `ec4c96963abf206ab2f2b0253a5efca71ba28043`
- Apache-2.0; see `LICENSE` and source copyright headers.
- Imported from the Apache-licensed third-party directory in Hyper-PiliPlus at
  `12f95eaf5d660a1c3ae5bb5e66c39984ddd73aac`:
  https://github.com/AritxOnly/Hyper-PiliPlus/tree/12f95eaf5d660a1c3ae5bb5e66c39984ddd73aac/android/third_party/miuix-glass

This is the material dependency closure, compiled with Miuix UI and blur
`0.9.4-rc01`. It is not an official published glass artifact or a claim that
Miuix has released a version named "HyperOS 4". Android API 33+ is required.

The imported adaptation splits `GlassStyles.kt` into a public forwarding object
and four internal token holders, retaining all token values. The original
`upstream-sha256.json` is retained as an upstream provenance record (it is not a
manifest of hashes for the split local files). No QQ-specific shader or optical
constant modifications have been made.

QQ navigation and theme integration are independently implemented in the app.
No GPL-licensed Hyper-PiliPlus/Deadliner navigation or Flutter code is included.
The existing Apache-2.0 project license remains unchanged.

Glass consumers must be outside the subtree recorded by their backdrop, to avoid
recursive sampling. Replace this directory with an official published artifact
only after verifying its API and visual compatibility.
