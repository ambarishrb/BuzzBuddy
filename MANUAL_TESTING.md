# BuzzBuddy Testing — Phase 4 of 4

**Current phase:** General code quality (Timber + strings)

---

## What Phase 4 covers

| Item | Goal |
| --- | --- |
| **4A Logging** | No `println` / `printStackTrace`. Timber only plants `DebugTree` in debug builds. |
| **4B Strings** | User-facing copy (toasts, buttons, titles, hints, settings) lives in `res/values/strings.xml`. |

---

## Test 1 — Logging (4A)

1. Open the app (debug build).
2. Set a 1–2 minute alarm.
3. In Logcat, filter by `Timber` / `BuzzBuddy`. You should see debug lines while the alarm schedules.
4. A **release** build should not spam those debug logs (DebugTree is debug-only).

**Pass**
- [ ] Debug Logcat shows Timber logs when scheduling.
- [ ] App does not crash.

---

## Test 2 — Main screens still show real copy (4B)

1. Alarm list: title **Alarms**, add button, next-alarm line.
2. Add alarm: dialog **Set Alarm**, **Cancel**, **Set**, name hint.
3. Delete + undo: **Alarm deleted** / **Undo**.
4. Duplicate time: **Alarm already set for this time!**
5. Settings: **Settings**, snooze duration, volume, vibrate, auto-dismiss.
6. Ringing screen: **Snooze** and **Dismiss**.

**Pass**
- [ ] No empty buttons or missing titles.
- [ ] Toasts/snackbars still readable.

---

## Test 3 — Time labels

1. Create an alarm at 00:00 and one after noon.
2. List should show **AM** / **PM** correctly.

**Pass**
- [ ] Midnight shows AM, afternoon shows PM.

---

## Phase 4 result

- [ ] **Passed** — production-readiness plan is complete  
- [ ] **Failed** — note which screen looked wrong
