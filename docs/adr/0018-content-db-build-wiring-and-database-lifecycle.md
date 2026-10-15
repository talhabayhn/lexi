# ADR 0018: content.db build wiring and database lifecycle

- Status: Accepted
- Date: 2026-10-15

## Context
ADR 0004 decided on a bundled read-only `content.db` and an on-device `user.db`. Open points: how the build
produces and bundles `content.db` (it is gitignored) on Android, iOS (`embedAndSignAppleFrameworkForXcode`) and CI
without a manual step; how the app installs it; how `user.db` evolves; and which libraries the data layer needs
for observable queries.

## Decision
- **Build:** `:tools:content-compiler:generateContentDb` (JavaExec; inputs `content/`, `$LEXI_PACKS_DIR`,
  `$SOURCE_DATE_EPOCH`, so it stays up to date until they change) writes `files/content.db` and
  `files/content-version.txt` into its build directory and publishes that directory as a consumable variant
  (`Usage = lexi-content-db`). `shared` resolves that variant (an
  artifact, not code; module rules unchanged), syncs it together with its own `src/commonMain/composeResources` into
  `build/generated/composeResourcesWithContent`, and registers that as the `commonMain` Compose resources directory
  (`compose.resources.customDirectory`). Compose then bundles it like any resource on Android and iOS. A content
  validation error fails the app build.
- **Install:** on first use (on the IO dispatcher) the app compares `content-version.txt` (no need to read the
  bundled DB) with the installed copy's `meta.contentVersion` and copies the bundle when they differ or the copy is
  unreadable: temp file in the same directory, stale `-wal`/`-shm`/`-journal` removed, atomic rename. Paths: Android
  `Context.getDatabasePath`, iOS `Application Support/databases` (SQLiter default). The copy is opened with
  `PRAGMA query_only = 1` (iOS also with a rollback journal); its `user_version` equals the schema version, so the
  drivers never create or migrate it.
- **user.db:** schema v1 in `.sq` files; every later change is `<from-version>.sqm` plus a frozen schema snapshot
  `core/data/src/jvmTest/resources/user-db/<version>.sql`. `UserDatabaseMigrationTest` migrates every snapshot to
  the current version and compares tables, columns and indexes with a fresh install (and keeps rows). SQL stays
  within SQLite 3.18 (Android 8, minSdk 26): no UPSERT, no window functions.
- **Libraries:** SQLDelight `coroutines-extensions` (same version as SQLDelight) for `Flow` queries in `core:data`;
  Koin in `core:data` for its module (ADR 0003); DataStore as in ADR 0015.

## Consequences
- No manual step anywhere: `./gradlew build`, `assembleDebug` and the Xcode build all regenerate `content.db` when
  content changes. `:tools:content-compiler:run` stays the manual tool (report; output under `build/content`).
- Downgrades (development only) re-copy the older bundle, so content and code always match.
- Alternatives not taken: committing `content.db` (binary diffs, stale DB), a project dependency from `shared` on
  the compiler's code (breaks module rules), reading the bundled DB's version by copying it every launch (slow),
  SQLDelight's `verifyMigrations` with binary `.db` schema files (not reviewable in diffs).
