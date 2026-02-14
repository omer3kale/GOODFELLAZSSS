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
 * Tests for CoCos 0xFC013 – 0xFC027.
 *
 * Each test parses a dedicated .fb model that isolates exactly one new CoCo
 * violation, runs the full checker, and verifies the expected error code
 * appears in {@link Log#getFindings()}.
 *
 * <p>Three CoCos cannot be triggered via .fb files because the grammar
 * enforces structural invariants at the parser level:
 * <ul>
 *   <li>0xFC015 (CountryHasAtLeastOneLeague) — grammar requires {@code League+}</li>
 *   <li>0xFC018 (NavigationNotEmpty) — grammar requires ≥ 1 {@code NavigationItem}</li>
 *   <li>0xFC019 (ScoreNonNegative) — {@code NatLiteral} is unsigned</li>
 * </ul>
 * These remain as defensive guards for programmatic AST construction.
 *
 * <p>0xFC027 (MaxMatchesPerLeague, > 380 matches) is impractical to test via
 * a .fb file; it is verified below using a programmatically generated model.
 *
 * <p>Total: 15 test methods (12 model-based + 1 programmatic + 2 all-pass).
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

    // ══════════════════════════════════════════════════════════════════
    // 0xFC013 — MatchDateWithinSeason
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testMatchDateOutOfSeason() throws IOException {
        ASTFootballSite ast = parseModel("football/invalid/MatchDateOutOfSeason.fb");
        long before = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertTrue("Should trigger CoCo errors", Log.getErrorCount() > before);
        assertTrue("Should contain 0xFC013", findingsContain("0xFC013"));
    }

    // ══════════════════════════════════════════════════════════════════
    // 0xFC014 — StadiumNameMinLength
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testShortStadiumName() throws IOException {
        ASTFootballSite ast = parseModel("football/invalid/ShortStadiumName.fb");
        long before = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertTrue("Should trigger CoCo errors", Log.getErrorCount() > before);
        assertTrue("Should contain 0xFC014", findingsContain("0xFC014"));
    }

    // ══════════════════════════════════════════════════════════════════
    // 0xFC016 — LeagueHasAtLeastOneMatch
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testEmptyLeague() throws IOException {
        ASTFootballSite ast = parseModel("football/invalid/EmptyLeague.fb");
        long before = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertTrue("Should trigger CoCo errors", Log.getErrorCount() > before);
        assertTrue("Should contain 0xFC016", findingsContain("0xFC016"));
    }

    // ══════════════════════════════════════════════════════════════════
    // 0xFC017 — UniqueMatchPerLeague
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testDuplicateMatch() throws IOException {
        ASTFootballSite ast = parseModel("football/invalid/DuplicateMatch.fb");
        long before = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertTrue("Should trigger CoCo errors", Log.getErrorCount() > before);
        assertTrue("Should contain 0xFC017", findingsContain("0xFC017"));
    }

    // ══════════════════════════════════════════════════════════════════
    // 0xFC020 — ScoreReasonableUpperBound
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testScoreUpperBound() throws IOException {
        ASTFootballSite ast = parseModel("football/invalid/ScoreUpperBound.fb");
        long before = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertTrue("Should trigger CoCo errors", Log.getErrorCount() > before);
        assertTrue("Should contain 0xFC020", findingsContain("0xFC020"));
    }

    // ══════════════════════════════════════════════════════════════════
    // 0xFC021 — CountryNameLengthLimit
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testLongCountryName() throws IOException {
        ASTFootballSite ast = parseModel("football/invalid/LongCountryName.fb");
        long before = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertTrue("Should trigger CoCo errors", Log.getErrorCount() > before);
        assertTrue("Should contain 0xFC021", findingsContain("0xFC021"));
    }

    // ══════════════════════════════════════════════════════════════════
    // 0xFC022 — LeagueNameLengthLimit
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testLongLeagueName() throws IOException {
        ASTFootballSite ast = parseModel("football/invalid/LongLeagueName.fb");
        long before = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertTrue("Should trigger CoCo errors", Log.getErrorCount() > before);
        assertTrue("Should contain 0xFC022", findingsContain("0xFC022"));
    }

    // ══════════════════════════════════════════════════════════════════
    // 0xFC023 — CityNameNotBlank
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testBlankCity() throws IOException {
        ASTFootballSite ast = parseModel("football/invalid/BlankCity.fb");
        long before = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertTrue("Should trigger CoCo errors", Log.getErrorCount() > before);
        assertTrue("Should contain 0xFC023", findingsContain("0xFC023"));
    }

    // ══════════════════════════════════════════════════════════════════
    // 0xFC024 — SeasonYearsConsecutive
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testNonConsecutiveSeason() throws IOException {
        ASTFootballSite ast = parseModel("football/invalid/NonConsecutiveSeason.fb");
        long before = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertTrue("Should trigger CoCo errors", Log.getErrorCount() > before);
        assertTrue("Should contain 0xFC024", findingsContain("0xFC024"));
    }

    // ══════════════════════════════════════════════════════════════════
    // 0xFC025 — MatchTimeGranularity
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testOddTimeMinutes() throws IOException {
        ASTFootballSite ast = parseModel("football/invalid/OddTimeMinutes.fb");
        long before = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertTrue("Should trigger CoCo errors", Log.getErrorCount() > before);
        assertTrue("Should contain 0xFC025", findingsContain("0xFC025"));
    }

    // ══════════════════════════════════════════════════════════════════
    // 0xFC026 — LeagueSeasonConsistentWithinCountry
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testMixedSeasons() throws IOException {
        ASTFootballSite ast = parseModel("football/invalid/MixedSeasons.fb");
        long before = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertTrue("Should trigger CoCo errors", Log.getErrorCount() > before);
        assertTrue("Should contain 0xFC026", findingsContain("0xFC026"));
    }

    // ══════════════════════════════════════════════════════════════════
    // 0xFC027 — MaxMatchesPerLeague (programmatic — 381 matches)
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testTooManyMatches() throws IOException {
        // Build a model with 381 matches to exceed the 380-match limit
        StringBuilder sb = new StringBuilder();
        sb.append("footballsite TooManyMatchesTest {\n");
        sb.append("  navigation { Testland; }\n");
        sb.append("  country Testland {\n");
        sb.append("    league BigLeague season \"2025-2026\" {\n");
        for (int i = 1; i <= 381; i++) {
            // Vary the date so each match is unique (0xFC017)
            int month = ((i - 1) / 28) + 1;   // 1..14, but months > 12 will
            int day   = ((i - 1) % 28) + 1;    // trigger 0xFC006 for months > 12
            // To avoid triggering 0xFC006, stay within valid months
            // Use 2025 and 2026 years, 12 months, 28 days → 12*28 = 336 + need 45 more
            int year;
            if (i <= 336) {
                year = 2025;
                month = ((i - 1) / 28) + 1;
                day   = ((i - 1) % 28) + 1;
            } else {
                year = 2026;
                int j = i - 336;
                month = ((j - 1) / 28) + 1;
                day   = ((j - 1) % 28) + 1;
            }
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
        sb.append("    }\n");
        sb.append("  }\n");
        sb.append("}\n");

        ASTFootballSite ast = parseString(sb.toString());
        long before = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertTrue("Should trigger CoCo errors", Log.getErrorCount() > before);
        assertTrue("Should contain 0xFC027", findingsContain("0xFC027"));
    }

    // ══════════════════════════════════════════════════════════════════
    // All-pass: TinyTest.fb should trigger zero errors with all 27 CoCos
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testAllCoCosPassOnTinyTest() throws IOException {
        ASTFootballSite ast = parseModel("football/valid/TinyTest.fb");
        long before = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertEquals("TinyTest.fb should pass all 27 CoCos", before, Log.getErrorCount());
    }

    // ══════════════════════════════════════════════════════════════════
    // All-pass: Bundesliga.fb should trigger zero errors with all 27 CoCos
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testAllCoCosPassOnBundesliga() throws IOException {
        ASTFootballSite ast = parseModel("football/valid/Bundesliga.fb");
        long before = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertEquals("Bundesliga.fb should pass all 27 CoCos", before, Log.getErrorCount());
    }
}
