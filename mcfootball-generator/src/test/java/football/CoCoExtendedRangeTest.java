package football;

import football.cocos.FootballSiteCoCos;
import football.footballsite._ast.ASTFootballSite;
import football.footballsite._parser.FootballSiteParser;
import de.se_rwth.commons.logging.Log;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Comprehensive tests for CoCos 0xFC013–0xFC027 (Phase 6).
 *
 * <h3>Test organisation</h3>
 * <ul>
 *   <li><strong>Positive tests</strong> – a small valid model satisfies the
 *       constraint; error count stays at zero.</li>
 *   <li><strong>Negative tests</strong> – a dedicated invalid model (or inline
 *       string) violates exactly one CoCo; error code appears in findings.</li>
 *   <li><strong>Teaching-focused tests</strong> – richer scenarios that
 *       illustrate boundary behaviour, complementary checks, and collisions
 *       for lecture use.</li>
 * </ul>
 *
 * <h3>Grammar-unreachable CoCos</h3>
 * Three CoCos cannot be triggered via .fb files because the grammar enforces
 * their invariants at parse time:
 * <ul>
 *   <li>0xFC015 (CountryHasAtLeastOneLeague) – grammar: {@code Country → League+}</li>
 *   <li>0xFC018 (NavigationNotEmpty) – grammar: {@code Navigation → NavigationItem+}</li>
 *   <li>0xFC019 (ScoreNonNegative) – {@code NatLiteral} is unsigned</li>
 * </ul>
 * These remain as defensive guards for programmatic AST construction.
 *
 * <h3>Collision pair (teaching example)</h3>
 * 0xFC007 (MatchTimeFormatIsValid) and 0xFC025 (MatchTimeGranularity) can both
 * fire on the same match when the time has an invalid hour AND non-standard
 * minutes (e.g. "25:10"). This is an intentional "double-failure" example,
 * NOT a bug.
 *
 * <p>Total: 33 test methods in this class.
 */
public class CoCoExtendedRangeTest {

    @BeforeClass
    public static void initLog() {
        Log.init();
        Log.enableFailQuick(false);
    }

    @Before
    public void resetLog() {
        Log.clearFindings();
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private ASTFootballSite parseModel(String resourcePath) throws IOException {
        String absPath = new File("src/test/resources/" + resourcePath).getAbsolutePath();
        FootballSiteParser parser = new FootballSiteParser();
        Optional<ASTFootballSite> opt = parser.parseFootballSite(absPath);
        assertTrue("Model should parse: " + resourcePath, opt.isPresent());
        return opt.get();
    }

    private ASTFootballSite parseString(String content) throws IOException {
        File tmp = File.createTempFile("coco_ext_", ".fb");
        tmp.deleteOnExit();
        Files.write(tmp.toPath(), content.getBytes(StandardCharsets.UTF_8));
        FootballSiteParser parser = new FootballSiteParser();
        Optional<ASTFootballSite> opt = parser.parseFootballSite(tmp.getAbsolutePath());
        assertTrue("Inline model should parse", opt.isPresent());
        return opt.get();
    }

    /** Check whether any Log finding contains the given substring. */
    private boolean findingsContain(String substring) {
        for (Object f : Log.getFindings()) {
            if (f.toString().contains(substring)) {
                return true;
            }
        }
        return false;
    }

    /** Count how many findings contain the given substring. */
    private int countFindings(String substring) {
        int count = 0;
        for (Object f : Log.getFindings()) {
            if (f.toString().contains(substring)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Build a minimal valid model. All fields satisfy every CoCo.
     * Callers can override individual fields via the overrides.
     */
    private String buildMinimalModel(String siteName, String season,
            String date, String time, String homeTeam, String homeCity,
            String awayTeam, String awayCity, int homeScore, int awayScore,
            String stadium) {
        return "footballsite " + siteName + " {\n"
             + "  navigation { Testland; }\n"
             + "  country Testland {\n"
             + "    league TestLeague season \"" + season + "\" {\n"
             + "      match {\n"
             + "        date    \"" + date + "\"\n"
             + "        time    \"" + time + "\"\n"
             + "        home    \"" + homeTeam + "\"    (\"" + homeCity + "\")\n"
             + "        away    \"" + awayTeam + "\"    (\"" + awayCity + "\")\n"
             + "        score   " + homeScore + " - " + awayScore + "\n"
             + "        stadium \"" + stadium + "\"\n"
             + "      }\n"
             + "    }\n"
             + "  }\n"
             + "}\n";
    }

    /** Shortcut: build a fully valid minimal model. */
    private String validModel(String name) {
        return buildMinimalModel(name, "2025-2026", "2025-09-01", "15:00",
                "Team Alpha", "CityA", "Team Beta", "CityB", 1, 0, "Test Arena");
    }

    // ══════════════════════════════════════════════════════════════════
    //  0xFC013 — MatchDateWithinSeason
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testMatchDateWithinSeason_positive() throws IOException {
        // Date "2025-09-01" is within season "2025-2026" → no error.
        ASTFootballSite ast = parseString(validModel("DateOkTest"));
        long before = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertEquals("In-range date should not trigger 0xFC013",
                before, Log.getErrorCount());
    }

    @Test
    public void testMatchDateWithinSeason_negative() throws IOException {
        ASTFootballSite ast = parseModel("football/invalid/MatchDateOutOfSeason.fb");
        long before = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertTrue("Out-of-range date should trigger errors",
                Log.getErrorCount() > before);
        assertTrue("Should contain 0xFC013", findingsContain("0xFC013"));
    }

    // ══════════════════════════════════════════════════════════════════
    //  0xFC014 — StadiumNameMinLength
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testStadiumNameMinLength_positive() throws IOException {
        // Stadium "Test Arena" (10 chars ≥ 3) → no error.
        ASTFootballSite ast = parseString(validModel("StadOkTest"));
        long before = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertEquals("Long stadium name should not trigger 0xFC014",
                before, Log.getErrorCount());
    }

    @Test
    public void testStadiumNameMinLength_negative() throws IOException {
        ASTFootballSite ast = parseModel("football/invalid/ShortStadiumName.fb");
        long before = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertTrue("Short stadium should trigger errors",
                Log.getErrorCount() > before);
        assertTrue("Should contain 0xFC014", findingsContain("0xFC014"));
    }

    // ══════════════════════════════════════════════════════════════════
    //  0xFC016 — LeagueHasAtLeastOneMatch
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testLeagueHasAtLeastOneMatch_positive() throws IOException {
        // validModel has 1 match → no error.
        ASTFootballSite ast = parseString(validModel("NonEmptyLeagueTest"));
        long before = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertEquals("Non-empty league should not trigger 0xFC016",
                before, Log.getErrorCount());
    }

    @Test
    public void testLeagueHasAtLeastOneMatch_negative() throws IOException {
        ASTFootballSite ast = parseModel("football/invalid/EmptyLeague.fb");
        long before = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertTrue("Empty league should trigger errors",
                Log.getErrorCount() > before);
        assertTrue("Should contain 0xFC016", findingsContain("0xFC016"));
    }

    // ══════════════════════════════════════════════════════════════════
    //  0xFC017 — UniqueMatchPerLeague
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testUniqueMatchPerLeague_positive() throws IOException {
        // Single match → no duplicates possible → no error.
        ASTFootballSite ast = parseString(validModel("UniqueMatchTest"));
        long before = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertEquals("Single-match league should not trigger 0xFC017",
                before, Log.getErrorCount());
    }

    @Test
    public void testUniqueMatchPerLeague_negative() throws IOException {
        ASTFootballSite ast = parseModel("football/invalid/DuplicateMatch.fb");
        long before = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertTrue("Duplicate match should trigger errors",
                Log.getErrorCount() > before);
        assertTrue("Should contain 0xFC017", findingsContain("0xFC017"));
    }

    // ══════════════════════════════════════════════════════════════════
    //  0xFC020 — ScoreReasonableUpperBound
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testScoreUpperBound_positive() throws IOException {
        // Score 1-0 (both ≤ 99) → no error.
        ASTFootballSite ast = parseString(validModel("ScoreOkTest"));
        long before = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertEquals("Normal score should not trigger 0xFC020",
                before, Log.getErrorCount());
    }

    @Test
    public void testScoreUpperBound_negative() throws IOException {
        ASTFootballSite ast = parseModel("football/invalid/ScoreUpperBound.fb");
        long before = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertTrue("Score > 99 should trigger errors",
                Log.getErrorCount() > before);
        assertTrue("Should contain 0xFC020", findingsContain("0xFC020"));
    }

    // ══════════════════════════════════════════════════════════════════
    //  0xFC021 — CountryNameLengthLimit
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testCountryNameLengthLimit_positive() throws IOException {
        // "Testland" (8 chars ≤ 40) → no error.
        ASTFootballSite ast = parseString(validModel("CountryLenOkTest"));
        long before = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertEquals("Short country name should not trigger 0xFC021",
                before, Log.getErrorCount());
    }

    @Test
    public void testCountryNameLengthLimit_negative() throws IOException {
        ASTFootballSite ast = parseModel("football/invalid/LongCountryName.fb");
        long before = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertTrue("Long country name should trigger errors",
                Log.getErrorCount() > before);
        assertTrue("Should contain 0xFC021", findingsContain("0xFC021"));
    }

    // ══════════════════════════════════════════════════════════════════
    //  0xFC022 — LeagueNameLengthLimit
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testLeagueNameLengthLimit_positive() throws IOException {
        // "TestLeague" (10 chars ≤ 40) → no error.
        ASTFootballSite ast = parseString(validModel("LeagueLenOkTest"));
        long before = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertEquals("Short league name should not trigger 0xFC022",
                before, Log.getErrorCount());
    }

    @Test
    public void testLeagueNameLengthLimit_negative() throws IOException {
        ASTFootballSite ast = parseModel("football/invalid/LongLeagueName.fb");
        long before = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertTrue("Long league name should trigger errors",
                Log.getErrorCount() > before);
        assertTrue("Should contain 0xFC022", findingsContain("0xFC022"));
    }

    // ══════════════════════════════════════════════════════════════════
    //  0xFC023 — CityNameNotBlank
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testCityNameNotBlank_positive() throws IOException {
        // City "CityA" is not blank → no error.
        ASTFootballSite ast = parseString(validModel("CityOkTest"));
        long before = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertEquals("Non-blank city should not trigger 0xFC023",
                before, Log.getErrorCount());
    }

    @Test
    public void testCityNameNotBlank_negative() throws IOException {
        ASTFootballSite ast = parseModel("football/invalid/BlankCity.fb");
        long before = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertTrue("Blank city should trigger errors",
                Log.getErrorCount() > before);
        assertTrue("Should contain 0xFC023", findingsContain("0xFC023"));
    }

    // ══════════════════════════════════════════════════════════════════
    //  0xFC024 — SeasonYearsConsecutive
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testSeasonYearsConsecutive_positive() throws IOException {
        // Season "2025-2026" (end = start+1) → no error.
        ASTFootballSite ast = parseString(validModel("SeasonOkTest"));
        long before = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertEquals("Consecutive season should not trigger 0xFC024",
                before, Log.getErrorCount());
    }

    @Test
    public void testSeasonYearsConsecutive_negative() throws IOException {
        ASTFootballSite ast = parseModel("football/invalid/NonConsecutiveSeason.fb");
        long before = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertTrue("Non-consecutive season should trigger errors",
                Log.getErrorCount() > before);
        assertTrue("Should contain 0xFC024", findingsContain("0xFC024"));
    }

    // ══════════════════════════════════════════════════════════════════
    //  0xFC025 — MatchTimeGranularity
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testMatchTimeGranularity_positive() throws IOException {
        // Time "15:00" (minutes = 00) → no error.
        ASTFootballSite ast = parseString(validModel("TimeGranOkTest"));
        long before = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertEquals("00-minute time should not trigger 0xFC025",
                before, Log.getErrorCount());
    }

    @Test
    public void testMatchTimeGranularity_negative() throws IOException {
        ASTFootballSite ast = parseModel("football/invalid/OddTimeMinutes.fb");
        long before = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertTrue("Odd minute time should trigger errors",
                Log.getErrorCount() > before);
        assertTrue("Should contain 0xFC025", findingsContain("0xFC025"));
    }

    // ══════════════════════════════════════════════════════════════════
    //  0xFC026 — LeagueSeasonConsistentWithinCountry
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testSeasonConsistency_positive() throws IOException {
        // Single league → consistency trivially holds → no error.
        ASTFootballSite ast = parseString(validModel("SeasonConsOkTest"));
        long before = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertEquals("Single-league country should not trigger 0xFC026",
                before, Log.getErrorCount());
    }

    @Test
    public void testSeasonConsistency_negative() throws IOException {
        ASTFootballSite ast = parseModel("football/invalid/MixedSeasons.fb");
        long before = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertTrue("Mixed seasons should trigger errors",
                Log.getErrorCount() > before);
        assertTrue("Should contain 0xFC026", findingsContain("0xFC026"));
    }

    // ══════════════════════════════════════════════════════════════════
    //  0xFC027 — MaxMatchesPerLeague
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testMaxMatchesPerLeague_positive() throws IOException {
        // 1 match (well under 380) → no error.
        ASTFootballSite ast = parseString(validModel("FewMatchesTest"));
        long before = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertEquals("Few matches should not trigger 0xFC027",
                before, Log.getErrorCount());
    }

    @Test
    public void testMaxMatchesPerLeague_negative() throws IOException {
        // Programmatically generate 381 matches to exceed the 380-match limit.
        StringBuilder sb = new StringBuilder();
        sb.append("footballsite TooManyMatchesTest {\n");
        sb.append("  navigation { Testland; }\n");
        sb.append("  country Testland {\n");
        sb.append("    league BigLeague season \"2025-2026\" {\n");
        for (int i = 1; i <= 381; i++) {
            int year = (i <= 336) ? 2025 : 2026;
            int j = (i <= 336) ? i : (i - 336);
            int month = ((j - 1) / 28) + 1;
            int day   = ((j - 1) % 28) + 1;
            String dateStr = String.format("%04d-%02d-%02d", year, month, day);
            sb.append("      match {\n");
            sb.append("        date    \"").append(dateStr).append("\"\n");
            sb.append("        time    \"15:00\"\n");
            sb.append("        home    \"Team A\" (\"CityA\")\n");
            sb.append("        away    \"Team B\" (\"CityB\")\n");
            sb.append("        score   1 - 0\n");
            sb.append("        stadium \"Test Arena\"\n");
            sb.append("      }\n");
        }
        sb.append("    }\n  }\n}\n");

        ASTFootballSite ast = parseString(sb.toString());
        long before = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertTrue("381 matches should trigger errors",
                Log.getErrorCount() > before);
        assertTrue("Should contain 0xFC027", findingsContain("0xFC027"));
    }

    // ══════════════════════════════════════════════════════════════════
    //  All-pass sanity checks
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testAllCoCosPassOnTinyTest() throws IOException {
        ASTFootballSite ast = parseModel("football/valid/TinyTest.fb");
        long before = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertEquals("TinyTest.fb should pass all 27 CoCos",
                before, Log.getErrorCount());
    }

    @Test
    public void testAllCoCosPassOnBundesliga() throws IOException {
        ASTFootballSite ast = parseModel("football/valid/Bundesliga.fb");
        long before = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertEquals("Bundesliga.fb should pass all 27 CoCos",
                before, Log.getErrorCount());
    }

    // ══════════════════════════════════════════════════════════════════
    //  TEACHING TEST 1 — SeasonYearsConsecutive valid + invalid in one
    // ══════════════════════════════════════════════════════════════════

    /**
     * Teaching: season "2025-2026" is consecutive (OK);
     * season "2025-2027" has a 2-year gap (triggers 0xFC024 only).
     */
    @Test
    public void testSeasonYearsConsecutive_ValidAndInvalid() throws IOException {
        // Part A: valid consecutive season
        {
            ASTFootballSite ast = parseString(
                    buildMinimalModel("ConsecOk", "2025-2026",
                            "2025-09-01", "15:00",
                            "Team A", "CityA", "Team B", "CityB",
                            1, 0, "Test Arena"));
            long before = Log.getErrorCount();
            FootballSiteCoCos.createChecker().checkAll(ast);
            assertEquals("2025-2026 should not fire 0xFC024",
                    before, Log.getErrorCount());
        }

        Log.clearFindings();

        // Part B: invalid non-consecutive season
        {
            ASTFootballSite ast = parseString(
                    buildMinimalModel("ConsecBad", "2025-2027",
                            "2025-09-01", "15:00",
                            "Team A", "CityA", "Team B", "CityB",
                            1, 0, "Test Arena"));
            long before = Log.getErrorCount();
            FootballSiteCoCos.createChecker().checkAll(ast);
            assertTrue("2025-2027 should fire 0xFC024",
                    Log.getErrorCount() > before);
            assertTrue(findingsContain("0xFC024"));
            // Ensure only 0xFC024 fires from the new range, not 0xFC013:
            // date "2025-09-01" year=2025 is within [2025,2027], so 0xFC013
            // does NOT trigger.
            assertFalse("0xFC013 should NOT fire here",
                    findingsContain("0xFC013"));
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  TEACHING TEST 2 — MatchTimeGranularity with all valid 15-min steps
    // ══════════════════════════════════════════════════════════════════

    /**
     * Teaching: four valid 15-minute increments produce no errors.
     * One non-standard time ("18:10") fires 0xFC025 only.
     */
    @Test
    public void testMatchTimeGranularity_Valid15MinSteps() throws IOException {
        // Part A: four valid times (00, 15, 30, 45)
        String[] validTimes = {"15:00", "18:15", "20:30", "21:45"};
        for (String t : validTimes) {
            Log.clearFindings();
            ASTFootballSite ast = parseString(
                    buildMinimalModel("TimeGran" + t.replace(":", ""),
                            "2025-2026", "2025-09-01", t,
                            "Team A", "CityA", "Team B", "CityB",
                            1, 0, "Test Arena"));
            long before = Log.getErrorCount();
            FootballSiteCoCos.createChecker().checkAll(ast);
            assertEquals(t + " should be valid granularity",
                    before, Log.getErrorCount());
        }

        Log.clearFindings();

        // Part B: invalid time "18:10" → 0xFC025 fires, 0xFC007 does NOT
        {
            ASTFootballSite ast = parseString(
                    buildMinimalModel("TimeGranBad", "2025-2026",
                            "2025-09-01", "18:10",
                            "Team A", "CityA", "Team B", "CityB",
                            1, 0, "Test Arena"));
            long before = Log.getErrorCount();
            FootballSiteCoCos.createChecker().checkAll(ast);
            assertTrue("18:10 should fire 0xFC025",
                    Log.getErrorCount() > before);
            assertTrue(findingsContain("0xFC025"));
            // 18:10 is a valid HH:MM format → 0xFC007 should NOT fire.
            assertFalse("0xFC007 should NOT fire for 18:10",
                    findingsContain("0xFC007"));
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  TEACHING TEST 3 — Country and league name length limits
    // ══════════════════════════════════════════════════════════════════

    /**
     * Teaching: exactly 40 chars is OK; 41 chars fires the length CoCo.
     * Tests both 0xFC021 (country) and 0xFC022 (league).
     */
    @Test
    public void testCountryAndLeagueNameLengthLimits() throws IOException {
        // 40-char country name → OK
        // Build name: Abcdefghij × 4 = 40 chars
        String name40 = "AbcdefghijAbcdefghijAbcdefghijAbcdefghij";
        assertEquals("Precondition: name40 is 40 chars", 40, name40.length());
        {
            String model = "footballsite LenTest40 {\n"
                    + "  navigation { " + name40 + "; }\n"
                    + "  country " + name40 + " {\n"
                    + "    league TestLeague season \"2025-2026\" {\n"
                    + "      match {\n"
                    + "        date \"2025-09-01\" time \"15:00\"\n"
                    + "        home \"Team A\" (\"CityA\")\n"
                    + "        away \"Team B\" (\"CityB\")\n"
                    + "        score 1 - 0 stadium \"Test Arena\"\n"
                    + "      }\n"
                    + "    }\n"
                    + "  }\n"
                    + "}\n";
            ASTFootballSite ast = parseString(model);
            long before = Log.getErrorCount();
            FootballSiteCoCos.createChecker().checkAll(ast);
            assertEquals("40-char country name should be OK",
                    before, Log.getErrorCount());
        }

        Log.clearFindings();

        // 41-char country name → 0xFC021
        {
            ASTFootballSite ast = parseModel("football/invalid/LongCountryName.fb");
            long before = Log.getErrorCount();
            FootballSiteCoCos.createChecker().checkAll(ast);
            assertTrue("41-char country name should fire 0xFC021",
                    findingsContain("0xFC021"));
        }

        Log.clearFindings();

        // 41-char league name → 0xFC022
        {
            ASTFootballSite ast = parseModel("football/invalid/LongLeagueName.fb");
            long before = Log.getErrorCount();
            FootballSiteCoCos.createChecker().checkAll(ast);
            assertTrue("41-char league name should fire 0xFC022",
                    findingsContain("0xFC022"));
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  TEACHING TEST 4 — CityNameNotBlank vs MatchFieldsNotEmpty
    // ══════════════════════════════════════════════════════════════════

    /**
     * Teaching: blank (" ") city triggers 0xFC023 but NOT 0xFC008.
     * Empty ("") city triggers 0xFC008 but NOT 0xFC023.
     * This illustrates the complementary design of the two CoCos.
     */
    @Test
    public void testCityNameNotBlankVsEmptyField() throws IOException {
        // Part A: city = " " (whitespace-only)
        // CityNameNotBlank checks: !city.isEmpty() && city.trim().isEmpty()
        // → " " is not empty, but trims to empty → 0xFC023 fires.
        // MatchFieldsNotEmpty checks: city.isEmpty() → " " is NOT empty → 0xFC008 does NOT fire.
        {
            ASTFootballSite ast = parseString(
                    buildMinimalModel("BlankCityTest", "2025-2026",
                            "2025-09-01", "15:00",
                            "Team A", " ", "Team B", "CityB",
                            1, 0, "Test Arena"));
            FootballSiteCoCos.createChecker().checkAll(ast);
            assertTrue("Blank city should fire 0xFC023",
                    findingsContain("0xFC023"));
            assertFalse("Blank city should NOT fire 0xFC008",
                    findingsContain("0xFC008"));
        }

        Log.clearFindings();

        // Part B: city = "" (truly empty)
        // MatchFieldsNotEmpty checks: city.isEmpty() → true → 0xFC008 fires.
        // CityNameNotBlank checks: city.isEmpty() → true → returns early → 0xFC023 does NOT fire.
        {
            ASTFootballSite ast = parseString(
                    buildMinimalModel("EmptyCityTest", "2025-2026",
                            "2025-09-01", "15:00",
                            "Team A", "", "Team B", "CityB",
                            1, 0, "Test Arena"));
            FootballSiteCoCos.createChecker().checkAll(ast);
            assertTrue("Empty city should fire 0xFC008",
                    findingsContain("0xFC008"));
            assertFalse("Empty city should NOT fire 0xFC023",
                    findingsContain("0xFC023"));
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  TEACHING TEST 5 — LeagueSeasonConsistency within a country
    // ══════════════════════════════════════════════════════════════════

    /**
     * Teaching: two leagues with the same season are OK.
     * Two leagues with different seasons fire 0xFC026.
     */
    @Test
    public void testLeagueSeasonConsistencyWithinCountry() throws IOException {
        // Part A: two leagues, both "2025-2026" → OK
        {
            String model = "footballsite ConsistOk {\n"
                    + "  navigation { Testland; }\n"
                    + "  country Testland {\n"
                    + "    league LeagueA season \"2025-2026\" {\n"
                    + "      match {\n"
                    + "        date \"2025-09-01\" time \"15:00\"\n"
                    + "        home \"Team A\" (\"CityA\")\n"
                    + "        away \"Team B\" (\"CityB\")\n"
                    + "        score 1 - 0 stadium \"Arena One\"\n"
                    + "      }\n"
                    + "    }\n"
                    + "    league LeagueB season \"2025-2026\" {\n"
                    + "      match {\n"
                    + "        date \"2025-10-01\" time \"18:00\"\n"
                    + "        home \"Team C\" (\"CityC\")\n"
                    + "        away \"Team D\" (\"CityD\")\n"
                    + "        score 2 - 2 stadium \"Arena Two\"\n"
                    + "      }\n"
                    + "    }\n"
                    + "  }\n"
                    + "}\n";
            ASTFootballSite ast = parseString(model);
            long before = Log.getErrorCount();
            FootballSiteCoCos.createChecker().checkAll(ast);
            assertEquals("Same-season leagues should not fire 0xFC026",
                    before, Log.getErrorCount());
        }

        Log.clearFindings();

        // Part B: mixed seasons → 0xFC026
        {
            ASTFootballSite ast = parseModel("football/invalid/MixedSeasons.fb");
            long before = Log.getErrorCount();
            FootballSiteCoCos.createChecker().checkAll(ast);
            assertTrue("Mixed seasons should fire 0xFC026",
                    findingsContain("0xFC026"));
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  TEACHING TEST 6 — MaxMatchesPerLeague boundary (380 vs 381)
    // ══════════════════════════════════════════════════════════════════

    /**
     * Teaching: 380 matches (the limit) is OK; 381 fires 0xFC027.
     * Both models are generated programmatically in-memory — no .fb files.
     * This illustrates programmatic AST construction and CoCo performance.
     */
    @Test
    public void testMaxMatchesPerLeagueBoundary() throws IOException {
        // Helper: build a model string with N matches
        java.util.function.IntFunction<String> modelWithNMatches = n -> {
            StringBuilder sb = new StringBuilder();
            sb.append("footballsite MatchCountTest {\n");
            sb.append("  navigation { Testland; }\n");
            sb.append("  country Testland {\n");
            sb.append("    league BigLeague season \"2025-2026\" {\n");
            for (int i = 1; i <= n; i++) {
                int year = (i <= 336) ? 2025 : 2026;
                int j = (i <= 336) ? i : (i - 336);
                int month = ((j - 1) / 28) + 1;
                int day   = ((j - 1) % 28) + 1;
                sb.append("      match {\n");
                sb.append("        date    \"")
                  .append(String.format("%04d-%02d-%02d", year, month, day))
                  .append("\"\n");
                sb.append("        time    \"15:00\"\n");
                sb.append("        home    \"Team A\" (\"CityA\")\n");
                sb.append("        away    \"Team B\" (\"CityB\")\n");
                sb.append("        score   1 - 0\n");
                sb.append("        stadium \"Test Arena\"\n");
                sb.append("      }\n");
            }
            sb.append("    }\n  }\n}\n");
            return sb.toString();
        };

        // Part A: 380 matches → OK
        {
            ASTFootballSite ast = parseString(modelWithNMatches.apply(380));
            long before = Log.getErrorCount();
            FootballSiteCoCos.createChecker().checkAll(ast);
            assertFalse("380 matches should NOT fire 0xFC027",
                    findingsContain("0xFC027"));
        }

        Log.clearFindings();

        // Part B: 381 matches → 0xFC027
        {
            ASTFootballSite ast = parseString(modelWithNMatches.apply(381));
            long before = Log.getErrorCount();
            FootballSiteCoCos.createChecker().checkAll(ast);
            assertTrue("381 matches should fire 0xFC027",
                    findingsContain("0xFC027"));
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  COLLISION DEMO — 0xFC007 + 0xFC025 on "25:10"
    // ══════════════════════════════════════════════════════════════════

    /**
     * Teaching (collision example): time "25:10" triggers BOTH:
     * <ul>
     *   <li>0xFC007 (MatchTimeFormatIsValid) — hour 25 is outside 00-23.</li>
     *   <li>0xFC025 (MatchTimeGranularity) — minutes 10 is not in {00,15,30,45}.</li>
     * </ul>
     * This is the intentional "double-failure" teaching example.
     * It is NOT a bug — the two CoCos check orthogonal aspects of the time.
     */
    @Test
    public void testCollisionDemo_TimeFormatAndGranularity() throws IOException {
        ASTFootballSite ast = parseString(
                buildMinimalModel("CollisionTest", "2025-2026",
                        "2025-09-01", "25:10",
                        "Team A", "CityA", "Team B", "CityB",
                        1, 0, "Test Arena"));
        FootballSiteCoCos.createChecker().checkAll(ast);

        // Both CoCos should fire
        assertTrue("0xFC007 should fire on hour 25",
                findingsContain("0xFC007"));
        assertTrue("0xFC025 should fire on minute 10",
                findingsContain("0xFC025"));

        // Exactly 2 errors from the time-related CoCos
        assertEquals("Exactly 1 finding for 0xFC007", 1, countFindings("0xFC007"));
        assertEquals("Exactly 1 finding for 0xFC025", 1, countFindings("0xFC025"));
    }
}

