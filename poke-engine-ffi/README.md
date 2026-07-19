# poke-engine + Kotlin, via UniFFI

Wraps [pmariglia/poke-engine](https://github.com/pmariglia/poke-engine) (the Rust
engine Foul Play uses) so its MCTS search is callable directly from Kotlin/JVM,
with no subprocess and no hand-ported battle mechanics.

Verified against poke-engine v0.0.47 by reading the actual source
(`src/mcts.rs`, `src/io.rs`, `src/genx/state.rs`) - this isn't guessed from docs.

## What's here

- `poke-engine-ffi/` - a small Rust crate (`cdylib`) that depends on
  `poke-engine` and exposes three functions via `#[uniffi::export]`:
  `best_move_mcts`, `legal_options`, `debug_roundtrip_state`.
- `uniffi-bindgen/` - the tiny helper binary UniFFI's own docs recommend for
  generating bindings in a multi-crate workspace.
- `kotlin-example/PokeEngineClient.kt` - how you'd call it from Kotlin.

## 1. Build the native library

You need a reasonably recent stable Rust (poke-engine uses `LazyLock`, which
needs 1.80+; if `rustc --version` is older, update via rustup).

```bash
cd poke-engine-ffi
cargo build --release -p poke-engine-ffi
```

This produces the native library at:
- Linux: `target/release/libpoke_engine_ffi.so`
- macOS: `target/release/libpoke_engine_ffi.dylib`
- Windows: `target/release/poke_engine_ffi.dll`

## 2. Generate the Kotlin bindings

```bash
cargo run -p uniffi-bindgen -- generate \
  --library target/release/libpoke_engine_ffi.so \
  --language kotlin \
  --out-dir bindings-out
```

(swap the `.so` for `.dylib`/`.dll` on Mac/Windows). Look in `bindings-out` -
you'll get something like `uniffi/poke_engine_ffi/poke_engine_ffi.kt`.

## 3. Wire it into your Kotlin/Gradle project

1. Copy the generated `.kt` file into your source set (or point Gradle at
   `bindings-out` as an extra source dir).
2. Add the JNA dependency UniFFI's Kotlin bindings rely on (plain JVM, not
   Android, so no `@aar` classifier):
   ```kotlin
   dependencies {
       implementation("net.java.dev.jna:jna:5.14.0")
   }
   ```
3. Make the native library discoverable at runtime - pick one:
   - `System.load("/absolute/path/to/libpoke_engine_ffi.so")` once at startup, or
   - put it on the JVM's `java.library.path` / `-Djna.library.path=...`, or
   - drop it under `src/main/resources/` following JNA's platform-folder
     convention if you want it bundled into your jar.

See `kotlin-example/PokeEngineClient.kt` for a full usage example. Note UniFFI
camelCases things for Kotlin: `best_move_mcts` becomes `bestMoveMcts`,
`move_choice` becomes `moveChoice`, etc.

## 4. The part that's actually more work than this FFI setup

`best_move_mcts` takes a poke-engine **state string** - not JSON, a bespoke
positional format (Pokémon fields separated by `,`, Pokémon separated by `=`,
sides/weather/terrain/etc. separated by `/`). The only authoritative spec is
the doctest on `State::deserialize` in poke-engine's `src/state.rs` - there's
no separate schema doc.

So the real remaining project is a Kotlin-side battle tracker: something that
connects to a Pokemon Showdown server, parses the protocol messages turn by
turn (this is what `poke-env`'s client representation does in Python), and
serializes your tracked state into this exact string format. That's a bigger
piece of work than everything above combined.

Build it test-first rather than debugging it via live search results: hand-
construct a couple of known states, round-trip them through
`debug_roundtrip_state` (included in the wrapper), and diff the output
against what you put in. That'll catch formatting mistakes long before you're
trying to figure out why the MCTS result looks wrong.

## Later: multi-threaded search / Android

- poke-engine also ships `perform_mcts_shared_tree` (`src/mcts_threaded.rs`)
  for multi-threaded search - not wrapped here, but trivial to add as a
  fourth `#[uniffi::export]` function if single-threaded search turns out to
  be your bottleneck.
- If this ever needs to run on Android/Kotlin Multiplatform rather than plain
  JVM, the same Rust crate works with Kotlin Multiplatform-flavored UniFFI
  bindgen forks (Gobley or the Trixnity/UbiqueInnovation one) instead of
  vanilla UniFFI - the Rust side above doesn't change.
