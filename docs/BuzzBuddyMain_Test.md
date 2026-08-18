# BuzzBuddy Main — Test Plan

Automated tests in this repo were expanded and should be run on every change. Everything that needs a **phone in your hand** is listed under **You must run**.

## Automated — run here / CI

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
./gradlew :app:assembleRelease
```

| Suite | Covers |
|---|---|
| `Phase1CriticalStabilizationTest` | Exact-alarm permission wiring |
| `MainFixContractTest` | NumberPicker API safety, settings save, edit/disabled, sound URIs, gitignore, restore actions |
| `Phase2BuildSecurityTest` | Minify, catalog, ProGuard |
| `Phase2GsonSettingsTest` | Settings JSON |
| `Phase3SchedulerAndUndoTest` | Schedule math, snooze vs daily, snackbar anchor, BootReceiver restore, undo restore |
| `Phase4CodeQualityTest` | Timber, strings, no println |
| Instrumented (device, if connected) | Launch, permissions APK, settings persist, BootReceiver enabled, Room undo id |

### You must run (instrumented, needs a device/emulator)

```bash
./gradlew :app:connectedDebugAndroidTest
```

Grant Alarms & reminders on API 31+ before this or `Phase1OnDeviceStabilizationTest` fails.

---

## You must run — functional (one Pixel-class device)

Install **release** APK if you can (`assembleRelease` still uses debug signing until you add a Play key).

**Note:** If this phone previously had the interview/backend build (Room schema v2), first launch of main wipes that local DB so the app can start. Re-create alarms once.

### CRUD

- [ ] F1 Empty list shows “No upcoming alarms”
- [ ] F2 Add 07:00 “Gym”; banner matches
- [ ] F3 Add 07:00 again → already set
- [ ] F4 23:59, 00:00, 12:00 AM, 12:00 PM labels
- [ ] F5 Edit 07:00 → 07:05; only new time fires
- [ ] F6 Edit a **disabled** alarm → must **not** ring
- [ ] F7 Edit onto another alarm’s time → blocked
- [ ] F8 Toggle off → no ring
- [ ] F9 Toggle on → rings
- [ ] F10 Swipe delete, wait through time → no ring
- [ ] F11 Undo before snackbar dies → still rings
- [ ] F12 Let snackbar die → no ring

### Delete snackbar vs + (P1-1)

- [ ] U1 Snackbar does not cover +
- [ ] U2 Tap + while snackbar is up → create dialog opens
- [ ] U3 Undo still works
- [ ] U4 Two rapid deletes; + never blocked

### Ringing

Set 2–3 minutes ahead, screen off unless noted.

- [ ] R-UI1 Locked: full-screen + sound
- [ ] R-UI2 Dismiss stops everything; tomorrow still scheduled
- [ ] R-UI3 Snooze N minutes (not rounded to clock minute)
- [ ] R-UI4 / R-UI5 Stop snooze from list and from notification
- [ ] R-UI6 Stop from ringing notification
- [ ] R-UI7 Auto-dismiss ON: stops ~2 min, stays enabled
- [ ] R-UI8 Volume 0 / 50 / 100
- [ ] R-UI9 Gradual ON vs OFF
- [ ] R-UI10 Vibrate ON in silent
- [ ] R-UI11 Sunrise vs Beep actually sound different
- [ ] R-UI12 Banner while snoozed shows snooze time

### Settings persistence

- [ ] S1 Snooze 10 → 15 survives process death
- [ ] S2–S4 Toggle vibrate / gradual / auto-dismiss by **switch thumb** and by **row**; survive kill

### Permissions

- [ ] P1 Deny notifications — no crash; document what still works
- [ ] P2 Deny exact alarms — create prompts; does not pretend scheduled
- [ ] P3 Deny full-screen intent — notification still usable
- [ ] P4 Grant later, new alarm rings
- [ ] P5 Grant later, old list alarms ring after opening the app (reschedule-on-start)

---

## You must run — reliability

Each: alarm 2–3 minutes ahead, **do not open the app** unless noted.

| ID | Scenario | Expected |
|---|---|---|
| R1 | Background | Rings |
| R2 | Force-stop | Rings (fail = OEM battery) |
| R3 | Reboot, unlock, do not open app | Daily alarms ring |
| R4 | Reboot, stay locked past alarm time | Should ring from cache; if not, first unlock then open app |
| R5 | Sideload new APK over old, do not open app | Still rings |
| R6 | Same, then open app | Self-heals |
| R7 | Timezone ±3h | Next **local** time |
| R8 | Set clock across alarm | Fires or reschedules |
| R9 | Battery Restricted | Still rings |
| R10 | Overnight Doze | Morning rings |
| R11 | DND / Bedtime / silent | Audible (grant DND access if not) |
| R12 | During call / media | Audible |
| R13 | Two alarms 1 minute apart | Hear both or second replaces first (known player singleton) |
| R14 | Snooze then reboot | Snooze restored if still in the future |
| R15 | 24h soak, 3 alarms | All fire next day |
| R16 | Backup restore to empty phone | List + actually registered |

**Minimum if one phone:** R1 + R3 + R5 + R10 + U1–U4.

### OEM (R2, R3, R5, R9, R10)

Pixel, Samsung, one Xiaomi/Oppo if you have them.

### API smoke

- [ ] API 24–28 emulator: **create alarm must not crash** (P0-2)
- [ ] API 33 notification prompt
- [ ] API 34+ full-screen settings

---

## Release (you)

- [ ] `assembleRelease` install + 3-minute ring
- [ ] Play upload key, not debug (Fix P0-3b)
- [ ] jks not in git (`git status` clean of keystores)
- [ ] Play Console exact-alarm + FGS mediaPlayback declarations
- [ ] Crash reporting on
