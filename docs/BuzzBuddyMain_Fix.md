# BuzzBuddy Main — Fix List

Product: **main branch only**. This file is the backlog plus implementation status after the 2026-08-18 pass.

## Done in code (you do not need to re-implement)

| ID | Item | What landed |
|---|---|---|
| P0-1 | App update kills alarms | `BootReceiver` handles `MY_PACKAGE_REPLACED`; `BuzzBuddyApp` + `MainActivity.onResume` call `AlarmRescheduler` |
| P0-2 | NumberPicker crash API 24–28 | `setPickerTextColor()` (API 29 + reflection fallback) |
| P0-3a | Signing files in git | `.gitignore` now has `*.jks` `*.keystore` `*.der` `*.pem` `keystore.properties` |
| P0-4 | Timezone / clock change | `TIMEZONE_CHANGED` + `TIME_CHANGED` restore |
| P1-1 | Delete snackbar covers + | `Snackbar.setAnchorView(addAlarmButton)` |
| P1-2 | Sound picker unused | Sunrise = system alarm URI; Beep = notification URI; prepare() has fallbacks |
| P1-3 | Settings switches not saving | `OnCheckedChangeListener` on the switch thumb and the row |
| P1-4 | Edit disabled alarm re-arms | Schedule only if `isEnabled` |
| P1-5 | Duplicate time on edit | `getAlarmByTimeExcluding` |
| P1-6 | Snooze lost on reboot | Reschedule snooze-until if still in the future, plus daily |
| P1-7 | Locked reboot | `directBootAware` receiver + device-protected `AlarmScheduleCache` |
| P1-8 | Battery killers | Banner + ignore-battery-optimizations prompt |
| P2-1 | README vs behavior | README/strings aligned (undo, 1–60 snooze, auto-dismiss ringing, sounds) |
| P2-2 | Next-alarm ignores snooze | Banner uses `nextDueMillis` including snooze |
| P2-3 | Shared notification id | `1000 + alarmId` |
| P2-4 | Sticky restart with no id | `START_NOT_STICKY` + stop without ringing |
| P2-5 | MediaPlayer crash | try/catch + URI fallbacks |
| P2-6 | Permission “Not now” | Persistent list banner |
| P2-8 | Swipe `NO_POSITION` | `bindingAdapterPosition` |
| P2-9 | Backup without reschedule | Backup rules include Room + settings; cold start reschedules |
| P2-10 | Edit `finish()` race | Await `updateAndWait` then finish |

## Still on you (cannot be finished in this environment)

### P0-3b Play signing — **you**
Release is still signed with the **debug** keystore so local `assembleRelease` installs. Before Play: replace with a real upload key / Play App Signing. Do **not** commit the jks.

### P1-7 verify locked reboot — **you + phone**
Code is there; confirm a reboot while the phone stays locked still rings (or document “rings after first unlock”).

### P1-8 OEM autostart — **you + Samsung/Xiaomi/Oppo**
Prompt exists; OEM “autostart” menus cannot be automated. Follow in-app banner, then R2/R9/R10 on those skins.

### P2-3 overlap policy — **you (product call)**
Two alarms one minute apart still share one `AlarmPlayer`. Second may replace first audio. Decide queue vs replace after you hear R13.

### P2-7 DND access — **you**
Channel has `setBypassDnd(true)`. Full DND override needs you to grant “Do Not Disturb access” once (`AlarmPermissionHelper.requestDndAccess` exists; not forced on every launch). Test R11.

### Play Console — **you**
Declare exact alarms as an alarm clock; justify `FOREGROUND_SERVICE_MEDIA_PLAYBACK`; Crash reporting / Vitals.

Device and soak tests: see `docs/BuzzBuddyMain_Test.md` section **You must run**.
