# poke-engine + Kotlin (UniFFI) — Setup From Zero

This assumes you have **nothing** Rust-related installed yet. By the end
you'll have a working `cdylib` wrapping `poke-engine`, Kotlin bindings
generated, and a JVM project that can call into it.

---

## 0. Prerequisites check

You need:
- A JDK (11+) and Gradle — for the Kotlin side
- `git`
- A C linker (comes with Xcode CLT on macOS, `build-essential` on Linux,
  MSVC Build Tools on Windows)

Check what you've got:

```bash
java -version
gradle -version   # or use ./gradlew once the project exists
git --version
```

---

## 1. Install Rust

Use `rustup` (not your OS package manager — poke-engine needs a recent
toolchain and rustup makes updating trivial):

```bash
curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh
source "$HOME/.cargo/env"
rustc --version
```

You need **1.80+** (poke-engine uses `LazyLock`). If `rustup` gave you
something older:

```bash
rustup update stable
rustup default stable
```

---

## 2. Create the project layout

```bash
mkdir poke-bot && cd poke-bot
mkdir -p poke-engine-ffi
```

Inside `poke-engine-ffi/`, this will be a small Cargo **workspace** with two
crates: the actual FFI cdylib, and the `uniffi-bindgen` helper binary.

```bash
cd poke-engine-ffi
cat > Cargo.toml << 'EOF'
[workspace]
members = ["poke-engine-ffi", "uniffi-bindgen"]
resolver = "2"
EOF
```

### 2a. The FFI crate

```bash
cargo new --lib poke-engine-ffi
```

Edit `poke-engine-ffi/poke-engine-ffi/Cargo.toml`:

```toml
[package]
name = "poke-engine-ffi"
version = "0.1.0"
edition = "2021"

[lib]
crate-type = ["cdylib", "lib"]

[dependencies]
poke-engine = "0.0.47"
uniffi = { version = "0.28", features = ["cli"] }

[build-dependencies]
uniffi = { version = "0.28", features = ["build"] }
```

Add a `build.rs` next to it:

```bash
cat > poke-engine-ffi/build.rs << 'EOF'
fn main() {
    uniffi::generate_scaffolding("src/lib.rs").unwrap();
}
EOF
```

(You'll fill in `src/lib.rs` with the `#[uniffi::export]` functions —
`best_move_mcts`, `legal_options`, `debug_roundtrip_state` — once you're
ready to write the actual wrapper logic. This README is just the scaffolding
so you can get to that point.)

### 2b. The uniffi-bindgen helper crate

This tiny binary is the officially recommended pattern for generating
bindings in a multi-crate workspace (avoids version drift between the
library's UniFFI version and the CLI tool's).

```bash
cd ..
cargo new uniffi-bindgen
```

Edit `poke-engine-ffi/uniffi-bindgen/Cargo.toml`:

```toml
[package]
name = "uniffi-bindgen"
version = "0.1.0"
edition = "2021"

[dependencies]
uniffi = { version = "0.28", features = ["cli"] }

[[bin]]
name = "uniffi-bindgen"
path = "src/main.rs"
```

Edit `poke-engine-ffi/uniffi-bindgen/src/main.rs`:

```rust
fn main() {
    uniffi::uniffi_bindgen_main()
}
```

---

## 3. First build (sanity check)

From `poke-engine-ffi/`:

```bash
cargo build --release -p poke-engine-ffi
```

This will take a while the first time (pulling + compiling `poke-engine` and
its dependencies). If it succeeds you'll have:

- Linux: `target/release/libpoke_engine_ffi.so`
- macOS: `target/release/libpoke_engine_ffi.dylib`
- Windows: `target/release/poke_engine_ffi.dll`

---

## 4. Generate Kotlin bindings

```bash
cargo run -p uniffi-bindgen -- generate \
  --library target/release/libpoke_engine_ffi.so \
  --language kotlin \
  --out-dir bindings-out
```

(swap `.so` for `.dylib`/`.dll` depending on your OS). You'll get:

```
bindings-out/uniffi/poke_engine_ffi/poke_engine_ffi.kt
```

---

## 5. Set up the Kotlin/Gradle side

Back at the project root (`poke-bot/`):

```bash
cd ..
gradle init --type kotlin-application
```

Add the JNA dependency to `build.gradle.kts` (plain JVM target — not
Android, so no `@aar` classifier):

```kotlin
dependencies {
    implementation("net.java.dev.jna:jna:5.14.0")
}
```

Copy the generated bindings into your source tree:

```bash
mkdir -p src/main/kotlin/uniffi/poke_engine_ffi
cp poke-engine-ffi/bindings-out/uniffi/poke_engine_ffi/poke_engine_ffi.kt \
   src/main/kotlin/uniffi/poke_engine_ffi/
```

Make the native lib discoverable at runtime — easiest option while
iterating, drop this near the top of your `main()`:

```kotlin
System.load("/absolute/path/to/poke-engine-ffi/target/release/libpoke_engine_ffi.so")
```

(Later, swap to bundling it under `src/main/resources/<platform-tag>/` so it
ships inside the jar — see JNA's platform-folder convention.)

---

## 6. Confirm the wiring works

Once you've written a minimal `#[uniffi::export] fn ping() -> String` (or
similar) in `src/lib.rs`, rebuild + regenerate + copy, then call it from
Kotlin to confirm the whole pipeline — Rust → UniFFI → Kotlin → JVM — is
actually connected before you touch any real battle logic.

```bash
# one-liner you'll run constantly during development:
cd poke-engine-ffi && \
cargo build --release -p poke-engine-ffi && \
cargo run -p uniffi-bindgen -- generate --library target/release/libpoke_engine_ffi.so --language kotlin --out-dir bindings-out && \
cp bindings-out/uniffi/poke_engine_ffi/poke_engine_ffi.kt ../src/main/kotlin/uniffi/poke_engine_ffi/ && \
cd ..
```

Worth saving that as `scripts/rebuild-ffi.sh` — you'll run it every time you
touch the Rust side.

---
