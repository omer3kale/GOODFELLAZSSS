# MCFootball — Owner Self-Check Report & QA Handover

**Date:** 2026-02-14 (updated 2026-02-14)  
**Branch:** `main`  
**Build System:** Gradle 7.6.4 / JDK 11 / MontiCore 7.7.0-SNAPSHOT  
**Test Framework:** JUnit 4.13.2 / JaCoCo 0.8.11

---

## Self-Check Report (Tasks 1–6)

### Task 1 — Grammar & CoCos Sanity ✅
- Clean build: `./gradlew :mcfootball-generator:clean :mcfootball-generator:build :mcfootball-generator:generateSiteProd`
- **Result:** BUILD SUCCESSFUL, 0 MontiCore errors, 0 warnings
- **CoCos:** "all checks passed" (27 CoCo checkers registered, 0xFC001–0xFC027)
- **Edge-case test:** Added temp match with heavy Unicode (`1. FC Köln` vs `Bayern München`, stadium `RheinEnergieStadion`) — parsed and CoCos passed. Removed after verification.

### Task 2 — Unicode & Special Characters ✅
- **Name token override verified:** Created temp model with `Österreich`, `España`, `Córdoba`, `Löwe` as grammar Name identifiers (not string literals). All parsed successfully, CoCos passed, 5 pages generated.
- **Generated HTML scan:** `München`, `Mönchengladbach`, `Düsseldorf`, `Köln`, `GOODFELLAZßS` all present and correct in output HTML.
- **UTF-8 encoding:** `<meta charset="UTF-8">` in all pages. `build.gradle` has `options.encoding = 'UTF-8'` for JavaCompile, Test, Javadoc.
- Temp test model removed after verification.

### Task 3 — Templates, Branding & 3-Color Layout ✅
- **Checked:** index.html, germany/index.html, germany/bundesliga/index.html
- **White background:** `body { background-color: #ffffff; }` ✓
- **Black header/footer:** `header, footer { background-color: #000000; }` ✓
- **Brand "GOODFELLAZßS":** Bold, red `#e60000`, uppercase — appears in header AND footer of all 21 pages ✓
- **Team names:** `.team-name { color: #1e90ff; }` (light blue) ✓
- **Scores:** `.score { color: #e60000; }` (red) ✓
- **No stray colors:** Only black, white, red, light blue, dark gray `#333333` for secondary text ✓
- **Responsive:** `max-width: 1100px`, `viewport` meta tag, `box-sizing: border-box` ✓

### Task 4 — Navigation, Links & Page Structure ✅
- **Automated link check:** 211 internal links crawled across 21 pages
- **Result:** 0 broken links
- **Navigation structure:** index → 5 countries → 3 leagues each, breadcrumb nav on league pages, brand links back to root

### Task 5 — Data Completeness & Correctness ✅
- **Total matches:** 85 (across 15 leagues, 5 countries)
- **Date range:** 2025-07-26 to 2026-02-09 — all ≤ 2026-02-14 ✓
- **Time range:** All valid HH:MM (00:00–23:59) ✓
- **Non-empty fields:** All home/away/team/city/stadium populated ✓
- **Score range:** All 0–9 ✓
- **No self-play:** No match where home == away ✓
- **No duplicates:** 0 duplicate match signatures ✓
- **Per-league breakdown:**
  - Tier 1 leagues (Bundesliga, PremierLeague, LaLiga, SerieA, Ligue1): 8 matches each
  - Tier 2 leagues: 5 matches each
  - Tier 3 leagues: 4 matches each

### Task 6 — CI / GitHub Readiness ✅
- **Git status:** Working tree clean (no uncommitted tracked files)
- **Untracked files:** `DESIGN.md`, `MIGRATION.md`, `docs/`, `scripts/`, `models/generated/Germany.fb`, `mcfootball-generator/output/` — legacy/auxiliary, not part of build
- **CI workflow:** `.github/workflows/pages.yml` — builds with JDK 11, runs `generateSiteProd`, deploys to GitHub Pages from `public/`
- **Recent commits:**
  1. `9aa701a` — Rename brand GOODFELLAZFß → GOODFELLAZßS + extend Name token for UTF-8 Latin
  2. `52ec247` — Add UTF-8 encoding directives
  3. `830812a` — Apply GOODFELLAZFß three-color branding
  4. `c1aa5e5` — Populate AllEurope.fb with 25/26 season matches
  5. `9c5bd08` — Security hardening + multi-environment setup
  6. `3a52eeb` — Initial mcfootball site + GitHub Pages workflow

---

## Self-Check Report (Tasks 12–17) — Code & Test Coverage

### Task 12 — Unit Test Coverage ✅
- **Test class:** `FootballSiteToolTest.java` — 19 tests
- **Scope:** Valid parsing (3), CoCo positive (2), CoCo negative (7), generator output (3), `toSlug()` utility (4)
- **Result:** 19/19 PASS
- **Coverage (handwritten code):**
  - `football.cocos` — see JaCoCo report for latest (27 CoCos now)
  - `football.generator` — 97% instructions / 100% branches
  - `football.FootballSiteTool` — 0% (uses `System.exit()`, untestable without refactoring)

### Task 13 — Golden-Master / Snapshot Tests ✅
- **Test class:** `GoldenMasterTest.java` — 6 tests
- **Scope:** Structural validation of generated HTML — index page structure, country page structure, league page structure, match count verification, brand presence, no old brand leakage ("GOODFELLAZFß")
- **Result:** 6/6 PASS
- **Baseline model:** `TinyTest.fb` (1 country, 1 league, 2 matches)

### Task 14 — Property-Based / Fuzz Tests ✅
- **Test class:** `GrammarFuzzTest.java` — 5 tests
- **Scope:** 20 random valid models (seeded Random(42)), 20 random CoCo runs, boundary scores (0-0, 9-9), boundary dates (Jan 1/Dec 31), heavy Unicode (`Köln`, `München`, `Córdoba`, `São Paulo`, `Łódź`)
- **Result:** 5/5 PASS — parser and CoCos never crash on random valid input

### Task 15 — Open-Source & License Risk Audit ✅
- **Output:** `OSS-NOTICE.md`
- **Dependencies audited:** 7 (5 runtime, 2 test-only)
- **Licenses found:** BSD-3-Clause (MontiCore, SE-Commons), Apache-2.0 (FreeMarker), EPL-2.0 (JUnit, JaCoCo)
- **Result:** No GPL/copyleft dependencies. No license conflicts. SNAPSHOT versions noted as risk for reproducibility.

### Task 16 — Error Handling & Failure Modes ✅
- **Test class:** `ErrorHandlingTest.java` — 6 tests
- **Scope:** Missing file handling, malformed model parsing, partial model parsing, empty file parsing, output directory creation, multi-error CoCo reporting (≥2 errors detected)
- **Result:** 6/6 PASS — all failure paths handled gracefully (no uncaught exceptions)

### Task 17 — Coverage Report & Risk Summary ✅
- **JaCoCo report:** `mcfootball-generator/build/reports/jacoco/test/html/`
- **Total tests:** 60 across 6 test classes — 60/60 PASS
- **Overall project coverage:** 15% instructions / 7% branches (includes massive MontiCore-generated code)
- **Handwritten code coverage:**

| Package              | Instructions | Branches |
|----------------------|-------------|----------|
| `football.cocos`     | 96%         | 85%      |
| `football.generator` | 97%         | 100%     |
| `football` (Tool)    | 0%          | 0%       |

### Additional 10 Domain Tests (ExtendedDomainTest.java) ✅
- **Test class:** `ExtendedDomainTest.java` — 10 tests
- **Result:** 10/10 PASS
- **Test models added:** 4 valid (`MultiCountry.fb`, `BoundaryScores.fb`, `LongTeamNames.fb`, `MixedUnicode.fb`) + 3 invalid (`InvalidNavCountry.fb`, `ShortSeason.fb`, `DuplicateLeague.fb`)
- **Test summary:**

| # | Test | Covers |
|---|------|--------|
| 1 | `testCrossCountryNavigationIntegrity` | Index → country → league link chain, all pages exist |
| 2 | `testLeagueMatchCountMatchesModel` | Rendered match rows = model match blocks (3, 2, 1) |
| 3 | `testBoundaryScoresAreAccepted` | Scores 0-0 and 9-9 pass CoCos and render |
| 4 | `testInvalidNavigationCountryTriggersCoCo` | 0xFC004 fires for undeclared "Portugal" |
| 5 | `testInvalidSeasonFormatCoCo` | 0xFC009 fires for "25-26" (not YYYY-YYYY) |
| 6 | `testTimeFormatBoundaryValues` | 00:00 OK, 23:45 OK, 24:00 triggers 0xFC007 |
| 7 | `testDuplicateLeagueNameCoCo` | 0xFC005 fires for two Bundesliga in one country |
| 8 | `testLongTeamNamesRenderCorrectly` | 72-char team names render, HTML stays valid |
| 9 | `testMixedUnicodeTeamNames` | DE/ES/FR/PT Unicode in identifiers + strings |
| 10 | `testBrandNameRegressionGOODFELLAZßS` | Brand ≥2× per HTML, no old brand leakage |

### CoCo Extended Range Tests (CoCoExtendedRangeTest.java) ✅
- **Test class:** `CoCoExtendedRangeTest.java` — 14 tests
- **Result:** 14/14 PASS
- **CoCo range extended:** 0xFC001–0xFC012 → 0xFC001–0xFC027 (15 new CoCos)
- **New invalid test models (11):** `MatchDateOutOfSeason.fb`, `ShortStadiumName.fb`, `EmptyLeague.fb`, `DuplicateMatch.fb`, `ScoreUpperBound.fb`, `LongCountryName.fb`, `LongLeagueName.fb`, `BlankCity.fb`, `NonConsecutiveSeason.fb`, `OddTimeMinutes.fb`, `MixedSeasons.fb`
- **Notes:** 0xFC015 (CountryHasAtLeastOneLeague) and 0xFC018 (NavigationNotEmpty) not testable via .fb — grammar enforces `Country → League+` and `Navigation → NavigationItem+`. 0xFC019 (ScoreNonNegative) not testable — `NatLiteral` is unsigned. 0xFC027 tested programmatically (381-match model).

| # | Test | CoCo Code | Covers |
|---|------|-----------|--------|
| 1 | `testMatchDateOutOfSeason` | 0xFC013 | Match year 2027 outside season 2025-2026 |
| 2 | `testShortStadiumName` | 0xFC014 | Stadium "AB" (2 chars < 3 min) |
| 3 | `testEmptyLeague` | 0xFC016 | League block with zero matches |
| 4 | `testDuplicateMatch` | 0xFC017 | Two matches with identical date/time/home/away |
| 5 | `testScoreUpperBound` | 0xFC020 | Score 100 exceeds 99 limit |
| 6 | `testLongCountryName` | 0xFC021 | Country name 41 chars (> 40 limit) |
| 7 | `testLongLeagueName` | 0xFC022 | League name 41 chars (> 40 limit) |
| 8 | `testBlankCity` | 0xFC023 | City " " is whitespace-only |
| 9 | `testNonConsecutiveSeason` | 0xFC024 | Season "2025-2027" (2-year gap) |
| 10 | `testOddTimeMinutes` | 0xFC025 | Time "15:07" not in {00,15,30,45} |
| 11 | `testMixedSeasons` | 0xFC026 | Two leagues with different seasons in one country |
| 12 | `testTooManyMatches` | 0xFC027 | 381 matches exceeds 380 limit |
| 13 | `testAllCoCosPassOnTinyTest` | all 27 | TinyTest.fb triggers 0 errors |
| 14 | `testAllCoCosPassOnBundesliga` | all 27 | Bundesliga.fb triggers 0 errors |

#### Complete CoCo Registry (27 codes)

| Code | Class | Scope | Description |
|------|-------|-------|-------------|
| 0xFC001 | `CountryNameIsUnique` | FootballSite | No duplicate country names |
| 0xFC002 | `LeagueNameStartUpperCase` | League | League name starts uppercase |
| 0xFC003 | `MatchHasTwoDifferentTeams` | Match | Home ≠ away team |
| 0xFC004 | `NavigationCountryExists` | FootballSite | Nav items reference real countries |
| 0xFC005 | `NoDuplicateLeaguePerCountry` | Country | No duplicate leagues per country |
| 0xFC006 | `MatchDateFormatIsValid` | Match | Date matches YYYY-MM-DD |
| 0xFC007 | `MatchTimeFormatIsValid` | Match | Time matches HH:MM (00-23:00-59) |
| 0xFC008 | `MatchFieldsNotEmpty` | Match | No empty team/city/stadium strings |
| 0xFC009 | `SeasonFormatIsValid` | League | Season matches YYYY-YYYY |
| 0xFC010 | `NavigationNoDuplicates` | Navigation | No duplicate nav items |
| 0xFC011 | `CountryNameStartUpperCase` | Country | Country name starts uppercase |
| 0xFC012 | `NavigationMatchesAllCountries` | FootballSite | Every country appears in nav |
| 0xFC013 | `MatchDateWithinSeason` | League | Match date year within season range |
| 0xFC014 | `StadiumNameMinLength` | Match | Stadium name ≥ 3 characters |
| 0xFC015 | `CountryHasAtLeastOneLeague` | Country | No empty country blocks |
| 0xFC016 | `LeagueHasAtLeastOneMatch` | League | No empty league blocks |
| 0xFC017 | `UniqueMatchPerLeague` | League | No duplicate (date,time,home,away) |
| 0xFC018 | `NavigationNotEmpty` | Navigation | Navigation has ≥ 1 country |
| 0xFC019 | `ScoreNonNegative` | Match | Scores ≥ 0 (defensive guard) |
| 0xFC020 | `ScoreReasonableUpperBound` | Match | Scores ≤ 99 |
| 0xFC021 | `CountryNameLengthLimit` | Country | Country name ≤ 40 characters |
| 0xFC022 | `LeagueNameLengthLimit` | League | League name ≤ 40 characters |
| 0xFC023 | `CityNameNotBlank` | Match | City not whitespace-only |
| 0xFC024 | `SeasonYearsConsecutive` | League | Season end year = start + 1 |
| 0xFC025 | `MatchTimeGranularity` | Match | Minutes in {00, 15, 30, 45} |
| 0xFC026 | `LeagueSeasonConsistentWithinCountry` | Country | All leagues share same season |
| 0xFC027 | `MaxMatchesPerLeague` | League | ≤ 380 matches per league |

---

## Risk Hotspots & Recommendations

### High-Risk Areas
1. **`FootballSiteTool.main()`** — 0% coverage. Uses `System.exit()` which prevents direct JUnit testing. **Recommendation:** Refactor to return exit codes instead of calling `System.exit()`, then add tests for CLI argument parsing, missing-file handling, and production vs. dev mode switching.
2. **CoCo branch coverage** — 27 CoCos now covered via `CoCoExtendedRangeTest` (14 tests) + existing negative tests. Most branches exercised. **Recommendation:** Inspect JaCoCo report for any remaining uncovered branches.
3. **SNAPSHOT dependencies** — `monticore-grammar:7.7.0-SNAPSHOT` and `se-commons:7.7.0-SNAPSHOT` are not reproducible releases. **Recommendation:** Pin to stable releases before production deployment.

### Medium-Risk Areas
4. **FreeMarker template errors** — Templates are not unit-tested for missing variable handling (e.g., if a league has 0 matches). **Recommendation:** Add a test with an empty league to verify template gracefully renders an empty table.
5. **Concurrent generation** — `FootballSiteGenerator` is not thread-safe. **Recommendation:** Document single-threaded usage constraint or add synchronization if parallel generation is planned.

### Low-Risk Areas
6. **Unicode edge cases** — Tested with Latin Extended (ä, ö, ü, ß, ñ, ó, ł) but not with CJK, emoji, or RTL scripts. **Recommendation:** If non-Latin leagues are planned, extend Name token further and add test models.
7. **Large dataset performance** — Tested with 85 matches (15 leagues). Not tested with 100+ leagues or 1000+ matches. **Recommendation:** Add a stress test if scaling is planned.

---

## QA Handover

### Scope
- **Site type:** Static HTML generated via MontiCore DSL + FreeMarker templates
- **Coverage:** 5 European countries × 3 leagues = 15 leagues, 85 matches, 21 HTML pages
- **Brand:** GOODFELLAZßS (three-color: white bg, black header/footer, red brand/scores, light blue team names)
- **Grammar:** MontiCore 7.7.0-SNAPSHOT with custom Name token supporting Unicode Latin Extended (U+00C0–U+017F)
- **Tests:** 46 unit tests (5 test classes), JaCoCo coverage, OSS license audit

### What Has Been Verified (Owner)
- Grammar builds and parses without errors or warnings
- All 12 CoCo constraint checkers pass
- Unicode renders correctly in HTML (umlauts, ß, accents)
- Extended Name token accepts Unicode identifiers (Österreich, Córdoba, etc.)
- Brand "GOODFELLAZßS" present on all 21 pages (header + footer)
- 3-color CSS theme consistent across index/country/league pages
- 211 internal links — 0 broken
- 85 matches — all valid dates, times, scores, no duplicates, no self-play
- CI workflow correct (`pages.yml`)
- **46 unit tests pass** (parsing, CoCos, generation, golden-master, fuzz, error handling, domain tests)
- **Code coverage:** 96–97% on handwritten cocos/generator code
- **OSS audit:** No license conflicts (see `OSS-NOTICE.md`)

### What QA Should Test Next
1. **Cross-browser rendering:** Chrome, Firefox, Safari, Edge — verify brand, colors, and Unicode characters render identically
2. **Mobile/responsive layout:** Test on phone-width screens (≤ 375px) — tables should scroll or adapt, header/footer should not overflow
3. **Accessibility:**
   - Screen reader traversal of navigation and match tables
   - Sufficient color contrast (red `#e60000` on white, white on black)
   - Keyboard navigation through all links
4. **GitHub Pages deployment:** After `git push`, confirm CI pipeline passes and deployed site is reachable
5. **Edge-case content:** Test with very long team names, scores at boundary (0-0, 9-9), and maximum Unicode density
6. **Performance:** Page load times for the index page with 5 country cards and league pages with 8+ match rows
7. **SEO/meta:** Verify `<title>` tags are descriptive, `<meta charset="UTF-8">` present, `<meta name="viewport">` present
8. **404 handling:** Navigate to a non-existent path and confirm GitHub Pages shows a reasonable error

---

*Generated by owner self-check on 2026-02-14. Tasks 12–17 added on 2026-02-14. 10 additional domain tests added on 2026-02-14. Ready for QA handoff.*
