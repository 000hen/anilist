# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working with code in this
repository.

## Commands

Run from the repository root — `gradlew.bat` from PowerShell, `./gradlew` from Git Bash.

| Task                                       | Command                                                                                                 |
|--------------------------------------------|---------------------------------------------------------------------------------------------------------|
| Debug build                                | `./gradlew assembleDebug`                                                                               |
| Install on a connected device              | `./gradlew installDebug`                                                                                |
| Release build (minified, resources shrunk) | `./gradlew assembleRelease`                                                                             |
| Lint                                       | `./gradlew lint` — `lintFix` applies the safe suggestions                                               |
| Unit tests                                 | `./gradlew testDebugUnitTest`                                                                           |
| One unit test                              | `./gradlew testDebugUnitTest --tests "one.muisnowdevs.apps.anilist.ExampleUnitTest.addition_isCorrect"` |
| Instrumented tests (needs a device)        | `./gradlew connectedDebugAndroidTest`                                                                   |

R8 keep rules go in `app/src/main/keepRules/`; the build merges every file in that directory and
there is no `proguard-rules.pro`.

No signing config is declared, so `assembleRelease` lands an **unsigned** `app-release-unsigned.apk`
under `app/build/outputs/apk/release/` and never touches the checked-in, signed
`app/release/app-release.apk`. That committed copy and its baseline profiles come from the IDE's
signed-build wizard, which is why they show up as working-tree changes.

## Architecture

One activity, one screen, no navigation graph and no dependency-injection container:
`MainActivity` → `AppLayout` (theme and top bar) → `AnimeScheduleScreen`. Everything shared is
reached through an `object` or a singleton rather than passed in, so a second screen means settling
both of those questions first.

### The schedule is scraped, not fetched

`source/YourAnimesSource.kt` is the entire data layer and the most brittle file here. It asks for
the *page* at `https://youranimes.tw/bangumi/{YYYYMM}` and mines the list out of the server-rendered
payload sitting in that page's inline scripts. `loadRaw` then:

1. takes the **second-to-last** `<script>self.__next_f.push(…)` block on the page,
2. strips the call wrapper and its trailing `)`,
3. parses that, takes element `[1]`, drops everything up to the first `:`, unescapes `\"` and `\\`,
   and drops the final three characters,
4. parses *that*, and walks the fixed path `[3].children[2][3].animes`.

Every index, offset and character count above is positional and unversioned. When the schedule comes
back wrong, assume the upstream page changed before looking for a bug in the callers — the `Log.d`
line after each cleaning stage is there to show which step drifted.

Nothing caches, but `MainViewModel.load` now catches: a decode failure lands in an `error` flow and
the screen offers a retry, rather than escaping `viewModelScope` and taking the app with it. That
matters because the season picker puts a page the site never published one tap away.
`CancellationException` is rethrown, and `isLoading` is cleared on every path except a cancelled
load, which leaves the flag to the load replacing it.

`source/Parser.kt` mirrors the upstream record and is deliberately forgiving — unknown keys ignored,
optional fields defaulted — so a *new* upstream field is harmless while a renamed required one
throws at decode time.

`AnimeSource` is the seam for a second provider, but `MainViewModel` names `YourAnimesSource`
directly, so swapping sources means editing the ViewModel.

### Airing times are minutes-since-Monday

A single integer space carries every time comparison:

- `WeekTime.week` is a `java.time.DayOfWeek`, so `MONDAY = 1` through `SUNDAY = 7`.
- `WeekTime.minute` is minutes past midnight, and `toZone` re-anchors both to another zone by
  walking back to the week's Monday, so a title crossing midnight also changes weekday.
- `WeekTime.minuteOfWeek = (week.value - 1) * 1440 + minute` places a title in the week;
  `getMinimalMinute()` and `getCurrentMinute()` place "start of today" and "now" in the same space.

The subtraction is what makes the space Monday-based. `getMinimalMinute()` reads
`dayOfWeek.value - 1` for the same reason — it used to step the enum back a day and read *that*
day's value, which is one lower every day but Monday, where it wrapped to Sunday's `7` and put the
start of today past the end of the week. Nothing tinted on Mondays.

`AnimeCard` compares those to tint a row that has already aired, and schedules a `delay` for the
remainder so a row flips while the list is open. Touching any of the three functions moves both the
sort order and that tint. The tint is also gated on `isCurrentSeason`, since a row carries a weekday
and a time but no date — in any other season the comparison describes today rather than the title.

`getTodayOrder()` is separate and rotates the day *list* so the schedule opens on today. It shares
`DayOfWeek`'s numbering but rotates a list that starts at Sunday, so its index is not
`minuteOfWeek`'s
— check which one a function speaks before reusing an index.

### Season maths lives in one place, and the ranges are one-based

`AnilistSeason.fromMonth()` matches `in 1..3 -> WINTER` through `in 10..12 -> FALL`, agreeing with
`java.time.LocalDate.monthValue` and with `AnilistSeason.month`. It is reached through
`AnilistSeasonYear.of(date)`, which `current()` calls with today.

The ranges were zero-based while every caller passed a one-based month, which put March, June and
September a season late and left December hitting `error("Invalid month")` — crashing the first
load. Two rival copies of this maths are gone with it: `getCurrentSessionString()` and the
`Endpoint.fetchList` default that called it. Keep it that way; a second copy is how the first one
drifted. `AnilistSeasonTest` pins all twelve months, and `AnilistSeasonYear.of` takes a date rather
than reading the clock so those months can be tested at all.

`AnilistSeasonYear.EARLIEST` and `latest()` bound what the season picker will step to.

### Favourites are one flow, hoisted the whole way up

`AnimeFavorite` is a process-wide singleton over shared preferences exposing
`StateFlow<Set<String>>`. `AnimeScheduleScreen` collects it once and hands both the set and the
setter down; no card, row or button keeps a flag of its own, which is what makes unstarring a row
drop it out of a filtered list on the spot. `showFavoritesOnly` lives in `MainActivity` because the
button that flips it sits in the app bar while the list it filters sits below — the activity is the
nearest thing owning both.

The selected season splits along the same seam but does not land in the same place. Only
`isPickerOpen` is the activity's; the season itself is `MainViewModel.selected`, because it is what
the load is addressed by — a copy held beside the app bar's chip could name one season while the
list below still held another. `MainActivity` therefore takes the view model and hands it to both
`AnimeScheduleScreen` and the bar, and pull-to-refresh calls `reload()` rather than working the
season out again, which is how it used to snap back to today's. `reload()` cancels whatever is in
flight first, or stepping through the picker lets a slow earlier response land last.

Hold new state the same way: components report, callers own. Which caller depends on what the state
is an input to, not on where the control sits.

## Conventions

- **Comments explain why, not what.** Nearly every non-trivial declaration carries a note justifying
  a decision — why `remember` and not `rememberSaveable`, why the star's colour sits outside the
  colour scheme, why a drag delta is accumulated before it is applied. Match that. A comment
  restating the code reads as out of place here.
- **UI copy is Traditional Chinese, written inline** in the composables (`"已收藏"`, `"官方連結"`,
  `"只顯示收藏"`); `strings.xml` holds only `app_name`. Content descriptions name the *action* a
  control performs, not the icon it shows.
- Colour comes from the scheme, which is dynamic from Android 12 up — so anything that must keep a
  fixed meaning is defined outside it. `FavoriteStar` in `ui/theme/Color.kt` is the existing case
  and the reason for adding another.
- `SwipeToFavoriteBox` is hand-rolled gesture code whose ordering constraints are subtle and
  documented in place — the deliberately dropped trailing delta, the latched `isCommitted`, the
  `finally`. Read those before touching the drag handlers.
- Streaming vendor badges are built by URL convention, `…/images/{vendor}_icon.webp`, and fall back
  to a generic play icon, so a missing or renamed vendor image looks like absent artwork rather than
  an error.
- Both test source sets still hold the generated project templates, so there is no existing coverage
  to model new tests on.
