# BuzzBuddy Production Readiness & Improvement Plan

This document outlines the necessary steps to stabilize **BuzzBuddy** for production release and improve overall code quality.

---

## 🔴 1. Critical Stabilization (Immediate Action Required)
These issues will cause crashes on modern Android versions (13+) or result in Play Store rejection.

### A. Exact Alarm Permission Strategy
*   **Issue:** `USE_EXACT_ALARM` is a restricted permission. Google Play will likely reject the app unless it is a core alarm/calendar app.
*   **Fix:** 
    *   Switch to `android.permission.SCHEDULE_EXACT_ALARM`.
    *   Add a check before scheduling: `if (alarmManager.canScheduleExactAlarms())`.
    *   Direct users to settings if permission is missing.

### B. SecurityException Prevention
*   **Issue:** Calling `setExactAndAllowWhileIdle` without the runtime permission check causes a crash on API 33+.
*   **Fix:** Wrap alarm scheduling in a safety check or handle the `SecurityException`.

### C. Lifecycle Safety
*   **Issue:** `launchWhenStarted` is deprecated and can lead to resource leaks or crashes when the fragment is in an unstable state.
*   **Fix:** Migrate to `viewLifecycleOwner.lifecycleScope.launch` with `repeatOnLifecycle(Lifecycle.State.STARTED)`.

---

## 🛠 2. Build & Security Enhancements

### A. Obfuscation & Shrinking
*   **Issue:** `isMinifyEnabled` is set to `false` for release builds.
*   **Fix:** Enable R8/ProGuard to protect business logic and reduce APK size. Add necessary Room and Gson rules to `proguard-rules.pro`.

### B. Dependency Cleanup
*   **Issue:** `build.gradle.kts` contains redundant declarations for `material`, `gson`, and `core-ktx` with conflicting versions.
*   **Fix:** 
    *   Remove hardcoded versions in `dependencies {}`.
    *   Centralize all versions in `gradle/libs.versions.toml`.
    *   Remove unused `FOREGROUND_SERVICE_CONNECTED_DEVICE` and `BLUETOOTH_SCAN` permissions.

---

## 🏗 3. Architectural Improvements

### A. Centralized Alarm Scheduler
*   **Issue:** Scheduling logic is duplicated in `SetAlarmPage.kt` and `BootReceiver.kt`.
*   **Fix:** Create a `BuzzBuddyAlarmScheduler` class to handle all `AlarmManager` interactions, ensuring consistency in Intent flags and `PendingIntent` IDs.

### B. Undo Logic Refinement
*   **Issue:** The "Undo" action creates a brand new alarm record (new ID).
*   **Fix:** Modify the repository to allow "Restoring" a deleted record or preserve the ID during the undo operation to keep the database clean.

---

## 📝 4. General Code Quality

### A. Robust Logging
*   **Issue:** Frequent use of `println` and `print` for debugging.
*   **Fix:** Implement `Timber` for release-safe logging or use the standard `Log` class with a consistent tag strategy.

### B. String Externalization
*   **Issue:** Hardcoded strings in fragments and adapters (e.g., "Alarm deleted", "Alarm already exists").
*   **Fix:** Move all user-facing strings to `res/values/strings.xml` for better maintainability and future localization support.
