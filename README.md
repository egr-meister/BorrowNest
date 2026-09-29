# BorrowNest

BorrowNest is a native Android, fully offline, borrowed-items tracker. It helps you keep a clear local record of items you **gave** to someone else and items you **borrowed** from someone else, using a distinctive two-sided **Given / Borrowed** return board.

BorrowNest is a manual item-tracking tool. It is **not** a financial lending app, money-loan tracker, credit or debt tool, rental marketplace, inventory platform, contact manager, messaging app, or legal-contract system.

## Main features

- Two-sided **Given / Borrowed** board with a central return spine.
- Manual records: item name, category, person label, record date, expected return date, status, priority, and notes.
- Derived date statuses: Active, Due soon, Due today, Overdue, No return date, Returned, Archived.
- Mark items returned, undo returns, archive and restore records, permanently delete archived records.
- In-app reminders for due-soon, due-today, overdue, and no-return-date items.
- Local history log, person summaries, neutral statistics, search, filters, and sorting.
- Works entirely offline with local-only storage. No account, no internet, no permissions.

## Manual tracking disclaimer

> BorrowNest is a manual item-tracking tool. Item details, names, dates, statuses, and notes are entered by the user. The app does not access contacts, verify ownership, create legal agreements, contact other people, or guarantee item return.

## Privacy note

> Names are entered manually and stored only on this device. BorrowNest does not access your contacts or upload personal data.

Full privacy statement:

> BorrowNest stores item names, manually entered person names, dates, statuses, notes, history, archive records, and settings locally on this device. The app has no account, no cloud sync, no internet access, no ads, no analytics, no payments, no contact access, no messaging, and no background monitoring.

## What BorrowNest does not do

BorrowNest does **not**: access your contacts, request `READ_CONTACTS` or any contact picker, collect phone numbers or emails, send SMS or email, message other people, create accounts, require authentication, sync between devices, use Firebase, connect to any backend or remote API, use cloud storage, show ads, run analytics, process payments, track money loans, calculate interest, create legal agreements, verify ownership or identity, run background services, use notifications/WorkManager/AlarmManager, or request any runtime permission. Person summaries are local groupings of manually entered labels — **they are not contacts** and similar names are never merged automatically.

## Given and Borrowed records

- **Given** means you gave an item to another person. The record shows the item, a "Given to" label, the person name, the date given, the expected return date, and status.
- **Borrowed** means you received an item temporarily from another person. The record shows the item, a "Borrowed from" label, the person name, the date borrowed, the return-by date, and status.

Wording is neutral throughout — no debt, collateral, claim, or liability language.

## Two-sided board

The Home screen is split vertically. The left lane is **Given** (outward-facing, warm terracotta); the right lane is **Borrowed** (inward-facing, cool indigo). A central return spine separates them and shows today's date, the nearest expected return, and due-today / overdue counts. Each lane has its own header with active and overdue counts, and item tiles flow downward. A bottom archive tray and bottom navigation (Board, History, Archive, Settings) complete the layout. A list-mode fallback (a Given/Borrowed segmented control) is available for narrow screens and accessibility.

## Item categories

Book, Tool, Clothing, Electronics, Household, Sports, Document, Accessory, Other. The **Other** category accepts an optional custom label.

## Manual name entry

Person names are entered as plain text (recommended max 100 characters, leading/trailing whitespace trimmed, international characters preserved). There are no contact suggestions, address-book lookups, phone/email fields, avatars, or share/message actions. Labels such as "Neighbor", "Office", "Cousin", or "Workshop" are welcome; a real full name is not required.

## Date handling

Dates are stored as `YYYY-MM-DD`, times as `HH:mm`, and timestamps as ISO-8601. All date logic uses `java.time.LocalDate` with core library desugaring, and reads the local device clock only (never network time). Invalid dates never crash the app — they fall back to friendly text ("Return date unavailable") and remain editable.

Record date is required. Expected return date is optional and may equal the record date. An expected return date earlier than the record date is only accepted after you explicitly confirm it as a historical/imported record.

## Derived status logic

Status is never stored; it is calculated in this order:

1. If archived → **Archived**.
2. If lifecycle is Returned → **Returned**.
3. If expected return date is empty → **No return date**.
4. If the date is invalid → **Return date unavailable** (InvalidDate).
5. If before today → **Overdue**.
6. If equals today → **Due today**.
7. If within the Soon threshold → **Due soon**.
8. Otherwise → **Active**.

- **Due soon**: active item whose expected return date is after today and within the configured Soon threshold (default 3 days; options 1, 3, 7, 14).
- **Due today**: active item whose expected return date equals today.
- **Overdue**: active item whose expected return date is before today.
- **No return date**: active item with no expected return date; never marked overdue.

## Mark returned workflow

Open a record, tap "Mark received back" (Given) or "Mark returned" (Borrowed), confirm or adjust the actual return date, optionally add a final note, and save. The item moves out of the active board into Returned/History, and archiving is suggested. A "This status is saved manually" note is shown; the app never claims another person confirmed a return.

## Undo return workflow

Undo clears the actual return date, restores the item to active, recalculates its date status, removes it from the archive if archived, and preserves notes and history. It requires confirmation ("Move this item back to active?").

## Archive

Returned records can be archived. Archived items leave the active board but remain in the Archive, where they can be filtered (All, Given, Borrowed, Year), restored, or permanently deleted after confirmation ("Delete this archived record permanently? This action cannot be undone.").

## History

History is an explicit, reverse-chronological log of events: Created, Updated, MarkedReturned, ReturnUndone, Archived, Restored. It can be filtered by All, Given, Borrowed, and Returned. History stores only user-provided item/person labels plus timestamps.

## Person summary

Person summaries group records by the exact, normalized person name (trim + case-insensitive for grouping only; the entered spelling is preserved). Each summary shows active Given/Borrowed counts, overdue count, returned count, and the latest related record. **These are not contact profiles**, no contact data is stored, and distinct names like "Alex" and "Alexander" are never merged automatically.

## Search and filters

Search matches item name, person name, category, and note text. Filters include Given, Borrowed, Active, Due Soon, Due Today, Overdue, No Return Date, Returned, High Priority, and Category. Sorting options: nearest return date, most overdue, newest created, oldest created, person name, item name. The active board default order is: overdue, due today, due soon, high priority, nearest return date, no return date.

## In-app reminders

Reminders appear **inside the app only**. They are evaluated when the app opens, when the board becomes active, and when records change. Types: due soon, due today, overdue, and active-with-no-return-date. High-priority items are raised within each group.

> BorrowNest reminders appear inside the app. The app does not send push notifications or contact other people.

There are **no** push notifications, no notification permission, and no background scheduling of any kind.

## Statistics

Neutral local summaries only: active Given/Borrowed, overdue Given/Borrowed, due today, no-return-date, returned this month, archived total, most-used category, and average active duration, plus a Given/Borrowed balance bar and a Compose-built monthly returned-count chart. BorrowNest never produces reliability, trust, risk, or debt scores and never rates people.

## Visual concept and layout uniqueness

The visual concept is the **Two-Sided Return Board** in a calm, paper-ledger "Dual-Lane Return Ledger" style. It deliberately avoids the generic "mascot → title → subtitle → stats card → button stack → settings" template. Instead it uses a genuine two-territory layout with a central spine, side-specific tiles, directional arrows, date tags, status chips, a central due marker, an archive tray, and bottom navigation. Given and Borrowed have distinct but coordinated palettes; red is reserved for overdue/errors/destructive actions and green only for returned/success. Status is always shown with text and icon, never color alone.

## App icon concept

A custom adaptive icon: a vertically split background (warm Given on the left, cool Borrowed on the right), a central return line, and two small opposing directional item tabs. No people, avatars, money, handshake, legal document, or text. A standalone vector fallback is provided for API 24–25.

## Splash screen concept

A stable static splash: a vertically split warm/cool background with a centered return-spine symbol and minimal opposing arrows. No people, contact, or money symbols, and no heavy animation.

## Technology stack

Kotlin, Jetpack Compose, Material 3, Navigation Compose, Android ViewModel, Kotlin Coroutines, Kotlin Flow, DataStore Preferences, Kotlinx Serialization, Gradle Kotlin DSL.

Not used: Retrofit, OkHttp, Ktor, Firebase, Room, SQLDelight, Realm, any networking/cloud/contact/image-loading library, chart libraries, or a dependency-injection framework.

## Architecture

Simple MVVM: one local repository (`BorrowRepository`) backed by DataStore Preferences, a single shared `BorrowViewModel` exposing immutable UI state via `StateFlow`, and focused date/status/reminder/filter/person/stats utilities. All app data is stored as three serialized JSON strings (`items_json`, `item_history_json`, `settings_json`). Deserialization is defensive: empty/missing/corrupted JSON falls back to safe defaults, unknown fields are ignored, missing fields use model defaults, and individual malformed items are salvaged where practical. Person names and notes are never logged in release builds, and full stored JSON is never logged.

## Requirements and configuration

- **JDK 17** is required.
- `compileSdk = 36`, `targetSdk = 36`, `minSdk = 24`.
- Portrait orientation is locked; the app is edge-to-edge and respects system insets while keeping system bars visible.
- **No permissions** are declared in the manifest (no INTERNET, READ_CONTACTS, camera, notifications, location, storage, calendar, Bluetooth, NFC, SMS, or alarm permissions).
- **16 KB page-size compatibility**: the app uses only Kotlin/Compose/AndroidX with no third-party native binaries, so 16 KB memory-page alignment is satisfied. Still verify the final bundle.

## Open in Android Studio

1. Open Android Studio (a recent stable version with AGP 8.6 support).
2. Choose **Open** and select the `BorrowNest` folder.
3. Let Gradle sync. Ensure the Gradle JDK is set to **17** (Settings → Build Tools → Gradle).
4. Run the `app` configuration on a device or emulator running API 24+.

> This project references Gradle 8.11.1 via the wrapper properties. If the `gradle/wrapper/gradle-wrapper.jar` and `gradlew`/`gradlew.bat` scripts are not present, generate them once with a local Gradle 8.11.1 install: `gradle wrapper --gradle-version 8.11.1`.

## Build instructions

Debug build:

```
./gradlew assembleDebug
```

Unit tests:

```
./gradlew testReleaseUnitTest
```

### Staged release build (R8)

First validate a **non-minified** release. In `app/build.gradle.kts` the release build type ships with:

```
isMinifyEnabled = false
isShrinkResources = false
```

Build, install, launch, and test it. Only after that is stable, flip both flags to `true` (keeping the provided `proguardFiles(...)`), then rebuild and re-test serialization, DataStore, navigation, date-status calculation, and the board layout.

## Release signing

Release APK and AAB must be signed with a **real PKCS12 keystore** — never the Android debug key.

Generate a keystore:

```
keytool -genkeypair -v -storetype PKCS12 -keystore borrownest-release-key.p12 -alias borrownest_key -keyalg RSA -keysize 2048 -validity 10000
```

### Local signing setup

Create a `keystore.properties` file in the project root (already git-ignored):

```
storeFile=/absolute/path/to/borrownest-release-key.p12
storePassword=YOUR_STORE_PASSWORD
keyAlias=borrownest_key
keyPassword=YOUR_KEY_PASSWORD
```

Alternatively, provide the same values through the environment variables `ANDROID_KEYSTORE_FILE`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, and `ANDROID_KEY_PASSWORD`. If release signing values are missing, the release build **fails clearly** rather than silently falling back to the debug key.

Never commit the keystore, decoded keystore, passwords, or `keystore.properties`.

### Required GitHub Secrets

- `ANDROID_KEYSTORE_BASE64` — base64 of your `.p12` keystore (`base64 -w0 borrownest-release-key.p12`).
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

Use the same password for the keystore and key unless you have configured separate values reliably.

## GitHub Actions

`.github/workflows/android-build.yml` runs on push to `main` and on manual dispatch. It checks out the repo, sets up JDK 17 and Android SDK Platform 36 + Build Tools 36.0.0, caches Gradle, runs unit tests, decodes `ANDROID_KEYSTORE_BASE64` into a temporary PKCS12 file, exposes signing secrets only as environment variables, builds the signed release **APK** and **AAB**, verifies the APK with `apksigner verify --print-certs`, **fails** if the certificate shows `CN=Android Debug`, and uploads the APK (test artifact) and AAB (Google Play artifact). CI proves compilation, signing, and certificate verification — it is not proof that the app launches. No emulator smoke test is required.

## apksigner verification

```
apksigner verify --print-certs app-release.apk
```

The printed signing certificate must **not** contain `CN=Android Debug`.

## Signed APK and AAB / Google Play

- Use the signed **APK** for local installation and verification.
- Use the signed **AAB** for Google Play. Only the `.aab` is uploaded to Google Play.

Outputs after a release build:

- APK: `app/build/outputs/apk/release/*.apk`
- AAB: `app/build/outputs/bundle/release/*.aab`

## Local release verification

1. Build the signed release APK.
2. Verify its certificate with `apksigner verify --print-certs`.
3. Install it: `adb install -r app-release.apk`.
4. Launch the app.
5. Inspect logs: `adb logcat`.
6. Complete the functional checklist below.
7. Repeat after enabling R8.

Watch logcat for `ClassNotFoundException`, `NoSuchMethodError`, serialization crashes, DataStore parse crashes, navigation-argument crashes, `LocalDate` parse crashes, missing-item crashes, invalid archive state, board-layout crashes, R8-related crashes, and signing misconfiguration.

## Local functional test checklist

Empty first launch; onboarding and skip-onboarding; add Given item; add Borrowed item; enter person name manually and confirm no contact picker exists; item without a return date; item with a future return date; item due today; overdue historical item; verify Due Soon / Due Today / Overdue; change the Soon threshold; edit item; change direction; change person name; change expected return date; add a note; mark Given item received back; mark Borrowed item returned; undo return; archive returned item; restore archived item; permanently delete archived item; search by item name and person name; filter Given / Borrowed / Overdue / No Return Date; sort by nearest return; open person summary and confirm similar names are not merged; open and filter history; open statistics; trigger due-soon / due-today / overdue in-app reminders and dismiss; disable reminders; archive all returned items; delete all Given records; delete all Borrowed records; reset all local data; relaunch the app; launch in airplane mode and confirm full functionality; confirm no INTERNET permission, no contact permission, no runtime permission dialogs, and no messaging behavior; inspect `adb logcat`; verify the release certificate; verify AAB generation; verify API 36; verify 16 KB page-size compatibility.

## Data reset behavior

Settings provides staged data controls: archive all returned items, clear archive, delete all Given records, delete all Borrowed records, and reset all local data. Each destructive action requires explicit confirmation. Reset removes every item, person label, date, status, note, history event, archive record, and setting stored by BorrowNest on this device.

## Manual-entry limitations

Everything in BorrowNest is entered by you. The app cannot detect returns automatically, cannot verify who owns an item or whether a handover occurred, cannot contact anyone, and makes no legal or financial claims. It is a private, offline personal record system for what you gave and what you borrowed.
