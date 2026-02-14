package football;

import football.cocos.FootballSiteCoCos;
import football.footballsite._ast.ASTFootballSite;
import football.footballsite._cocos.FootballSiteCoCoChecker;
import football.footballsite._parser.FootballSiteParser;
import football.generator.FootballSiteGenerator;
import de.se_rwth.commons.logging.Log;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * 10 additional domain-specific tests for MCFootball.
 *
 * <pre>
 *  1  testCrossCountryNavigationIntegrity    — nav links ↔ country pages ↔ league pages
 *  2  testLeagueMatchCountMatchesModel       — rendered match rows = model match blocks
 *  3  testBoundaryScoresAreAccepted          — scores 0-0 and 9-9 pass CoCos + render
 *  4  testInvalidNavigationCountryTriggersCoCo — 0xFC004 for undeclared nav country
 *  5  testInvalidSeasonFormatCoCo            — 0xFC009 for "25-26"
 *  6  testTimeFormatBoundaryValues           — 00:00 OK, 23:59 OK, 24:00 fails 0xFC007
 *  7  testDuplicateLeagueNameCoCo            — 0xFC005 for two Bundesliga in one country
 *  8  testLongTeamNamesRenderCorrectly       — extremely long team names survive pipeline
 *  9  testMixedUnicodeTeamNames              — Mönchengladbach, Atlético, São Paulo, Köln
 * 10  testBrandNameRegressionGOODFELLAZßS    — brand present ≥2×, old brand absent
 * </pre>
 */
public class ExtendedDomainTest {

    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

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

    private String readFile(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private int countOccurrences(String text, String sub) {
        int count = 0, idx = 0;
        while ((idx = text.indexOf(sub, idx)) != -1) {
            count++;
            idx += sub.length();
        }
        return count;
    }

    /** Check if any log finding contains the given substring. */
    private boolean findingsContain(String substring) {
        for (Object f : Log.getFindings()) {
            if (f.toString().contains(substring)) {
                return true;
            }
        }
        return false;
    }

    /** Build a minimal valid model with a specific time value. */
    private String buildModelWithTime(String siteName, String time) {
        return "footballsite " + siteName + " {\n"
             + "  navigation { Testland; }\n"
             + "  country Testland {\n"
             + "    league TestLeague season \"2025-2026\" {\n"
             + "      match {\n"
             + "        date    \"2025-09-01\"\n"
             + "        time    \"" + time + "\"\n"
             + "        home    \"Team A\"    (\"CityA\")\n"
             + "        away    \"Team B\"    (\"CityB\")\n"
             + "        score   1 - 0\n"
             + "        stadium \"Arena\"\n"
             + "      }\n"
             + "    }\n"
             + "  }\n"
             + "}\n";
    }

    private ASTFootballSite parseString(String modelContent) throws IOException {
        File tmpFile = File.createTempFile("ext_domain_", ".fb");
        tmpFile.deleteOnExit();
        Files.write(tmpFile.toPath(), modelContent.getBytes(StandardCharsets.UTF_8));
        FootballSiteParser parser = new FootballSiteParser();
        Optional<ASTFootballSite> opt = parser.parseFootballSite(tmpFile.getAbsolutePath());
        assertTrue("Inline model should parse", opt.isPresent());
        return opt.get();
    }

    // ══════════════════════════════════════════════════════════════════
    // Test 1 — Cross-country navigation integrity
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testCrossCountryNavigationIntegrity() throws IOException {
        ASTFootballSite ast = parseModel("football/valid/MultiCountry.fb");
        Path outDir = tempDir.newFolder("nav-integrity").toPath();
        new FootballSiteGenerator(outDir).generate(ast);

        // Index page links to both countries
        String indexHtml = readFile(outDir.resolve("index.html"));
        assertTrue("Index should link to Germany",
                indexHtml.contains("germany/index.html"));
        assertTrue("Index should link to Spain",
                indexHtml.contains("spain/index.html"));

        // Country pages exist and have correct league links
        assertTrue("germany/index.html should exist",
                outDir.resolve("germany/index.html").toFile().exists());
        assertTrue("spain/index.html should exist",
                outDir.resolve("spain/index.html").toFile().exists());

        String germanyHtml = readFile(outDir.resolve("germany/index.html"));
        assertTrue("Germany page should link to bundesliga",
                germanyHtml.contains("bundesliga/index.html"));
        assertTrue("Germany page should link to zweiteliga",
                germanyHtml.contains("zweiteliga/index.html"));

        String spainHtml = readFile(outDir.resolve("spain/index.html"));
        assertTrue("Spain page should link to la-liga",
                spainHtml.contains("la-liga/index.html"));
        assertTrue("Spain page should link to segunda-division",
                spainHtml.contains("segunda-division/index.html"));

        // All league pages actually exist
        assertTrue(outDir.resolve("germany/bundesliga/index.html").toFile().exists());
        assertTrue(outDir.resolve("germany/zweiteliga/index.html").toFile().exists());
        assertTrue(outDir.resolve("spain/la-liga/index.html").toFile().exists());
        assertTrue(outDir.resolve("spain/segunda-division/index.html").toFile().exists());
    }

    // ══════════════════════════════════════════════════════════════════
    // Test 2 — League match count expectations
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testLeagueMatchCountMatchesModel() throws IOException {
        ASTFootballSite ast = parseModel("football/valid/MultiCountry.fb");
        Path outDir = tempDir.newFolder("match-count").toPath();
        new FootballSiteGenerator(outDir).generate(ast);

        // Bundesliga: 3 matches → 6 team-name spans (2 per match)
        String buliHtml = readFile(outDir.resolve("germany/bundesliga/index.html"));
        assertEquals("Bundesliga should have 3 matches (6 team-name spans)",
                6, countOccurrences(buliHtml, "class=\"team-name\""));

        // LaLiga: 2 matches → 4 team-name spans
        String laligaHtml = readFile(outDir.resolve("spain/la-liga/index.html"));
        assertEquals("LaLiga should have 2 matches (4 team-name spans)",
                4, countOccurrences(laligaHtml, "class=\"team-name\""));

        // SegundaDivision: 1 match → 2 team-name spans
        String segundaHtml = readFile(outDir.resolve("spain/segunda-division/index.html"));
        assertEquals("SegundaDivision should have 1 match (2 team-name spans)",
                2, countOccurrences(segundaHtml, "class=\"team-name\""));
    }

    // ══════════════════════════════════════════════════════════════════
    // Test 3 — Boundary score values (0-0, 9-9)
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testBoundaryScoresAreAccepted() throws IOException {
        ASTFootballSite ast = parseModel("football/valid/BoundaryScores.fb");

        // CoCos should pass
        long errorsBefore = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertEquals("Boundary scores 0-0 and 9-9 should pass CoCos",
                errorsBefore, Log.getErrorCount());

        // Generator should produce output without crashing
        Path outDir = tempDir.newFolder("boundary-scores").toPath();
        new FootballSiteGenerator(outDir).generate(ast);

        String html = readFile(outDir.resolve("testland/test-league/index.html"));

        // Both matches should be rendered
        assertEquals("Should have 2 matches (4 team-name spans)",
                4, countOccurrences(html, "class=\"team-name\""));

        // Score class should appear
        assertTrue("Should contain score class", html.contains("class=\"score\""));
    }

    // ══════════════════════════════════════════════════════════════════
    // Test 4 — Invalid navigation country (0xFC004)
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testInvalidNavigationCountryTriggersCoCo() throws IOException {
        ASTFootballSite ast = parseModel("football/invalid/InvalidNavCountry.fb");

        long errorsBefore = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        long newErrors = Log.getErrorCount() - errorsBefore;

        // Should trigger exactly 1 CoCo error for undeclared "Portugal"
        assertEquals("Should trigger exactly 1 CoCo error", 1, newErrors);

        // Verify the specific error code and country name
        assertTrue("Should contain error 0xFC004", findingsContain("0xFC004"));
        assertTrue("Error should mention 'Portugal'", findingsContain("Portugal"));
    }

    // ══════════════════════════════════════════════════════════════════
    // Test 5 — Invalid season format (0xFC009)
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testInvalidSeasonFormatCoCo() throws IOException {
        ASTFootballSite ast = parseModel("football/invalid/ShortSeason.fb");

        long errorsBefore = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        long newErrors = Log.getErrorCount() - errorsBefore;

        // "25-26" violates YYYY-YYYY → exactly 1 CoCo error
        assertEquals("Short season should trigger exactly 1 CoCo error", 1, newErrors);

        // Verify the specific error code
        assertTrue("Should contain error 0xFC009", findingsContain("0xFC009"));
    }

    // ══════════════════════════════════════════════════════════════════
    // Test 6 — Time format edge cases (00:00, 23:59, 24:00)
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testTimeFormatBoundaryValues() throws IOException {
        // Sub-test A: "00:00" is valid
        {
            ASTFootballSite ast = parseString(buildModelWithTime("MidnightTest", "00:00"));
            long before = Log.getErrorCount();
            FootballSiteCoCos.createChecker().checkAll(ast);
            assertEquals("00:00 should be a valid time", before, Log.getErrorCount());
        }

        Log.clearFindings();

        // Sub-test B: "23:59" is valid
        {
            ASTFootballSite ast = parseString(buildModelWithTime("LateNightTest", "23:59"));
            long before = Log.getErrorCount();
            FootballSiteCoCos.createChecker().checkAll(ast);
            assertEquals("23:59 should be a valid time", before, Log.getErrorCount());
        }

        Log.clearFindings();

        // Sub-test C: "24:00" is invalid — triggers 0xFC007
        {
            ASTFootballSite ast = parseString(buildModelWithTime("InvalidTimeTest", "24:00"));
            long before = Log.getErrorCount();
            FootballSiteCoCos.createChecker().checkAll(ast);
            assertTrue("24:00 should trigger CoCo error",
                    Log.getErrorCount() > before);
            assertTrue("Should contain error 0xFC007 for invalid time",
                    findingsContain("0xFC007"));
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // Test 7 — Duplicate league name (0xFC005)
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testDuplicateLeagueNameCoCo() throws IOException {
        ASTFootballSite ast = parseModel("football/invalid/DuplicateLeague.fb");

        long errorsBefore = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);

        assertTrue("Duplicate league should trigger CoCo error",
                Log.getErrorCount() > errorsBefore);

        // Verify the specific error code
        assertTrue("Should contain error 0xFC005", findingsContain("0xFC005"));

        // Verify the error mentions "Bundesliga"
        assertTrue("Error should mention 'Bundesliga'",
                findingsContain("Bundesliga"));
    }

    // ══════════════════════════════════════════════════════════════════
    // Test 8 — Long team names and layout robustness
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testLongTeamNamesRenderCorrectly() throws IOException {
        ASTFootballSite ast = parseModel("football/valid/LongTeamNames.fb");

        // CoCos should pass
        long errorsBefore = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertEquals("Long team names should pass CoCos",
                errorsBefore, Log.getErrorCount());

        // Generate HTML
        Path outDir = tempDir.newFolder("long-names").toPath();
        new FootballSiteGenerator(outDir).generate(ast);

        String html = readFile(outDir.resolve("testland/test-league/index.html"));

        // Long names must appear in full, inside team-name spans
        assertTrue("Should contain long home team name",
                html.contains("Sporting Club VeryLongNameWithSpacesAndUmlauts München Twelve Extra Words"));
        assertTrue("Should contain long away team name",
                html.contains("FC ReallyLongTeamNameThatGoesOnForever Frankfurt"));
        assertTrue("Should contain team-name class",
                html.contains("class=\"team-name\""));

        // HTML structure should remain valid (no unclosed tags)
        assertTrue("HTML should have closing body tag", html.contains("</body>"));
        assertTrue("HTML should have closing html tag", html.contains("</html>"));
    }

    // ══════════════════════════════════════════════════════════════════
    // Test 9 — Mixed-language Unicode stress test
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testMixedUnicodeTeamNames() throws IOException {
        ASTFootballSite ast = parseModel("football/valid/MixedUnicode.fb");

        // CoCos should pass
        long errorsBefore = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertEquals("Mixed Unicode model should pass CoCos",
                errorsBefore, Log.getErrorCount());

        // Generate HTML
        Path outDir = tempDir.newFolder("mixed-unicode").toPath();
        new FootballSiteGenerator(outDir).generate(ast);

        // Country and league pages should exist with Unicode slugs
        assertTrue("österreich directory should exist",
                outDir.resolve("österreich/index.html").toFile().exists());
        assertTrue("süperlig league page should exist",
                outDir.resolve("österreich/süperlig/index.html").toFile().exists());

        String html = readFile(outDir.resolve("österreich/süperlig/index.html"));

        // Verify each Unicode name appears
        assertTrue("Should contain German: Mönchengladbach",
                html.contains("Mönchengladbach"));
        assertTrue("Should contain Spanish: Atlético Madrid",
                html.contains("Atlético Madrid"));
        assertTrue("Should contain Portuguese: São Paulo",
                html.contains("São Paulo"));
        assertTrue("Should contain German: Köln",
                html.contains("Köln"));
        assertTrue("Should contain French: Saint-Étienne",
                html.contains("Saint-Étienne"));
        assertTrue("Should contain Portuguese: Estádio da Luz",
                html.contains("Estádio da Luz"));
    }

    // ══════════════════════════════════════════════════════════════════
    // Test 10 — Regression test for GOODFELLAZßS branding
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testBrandNameRegressionGOODFELLAZßS() throws IOException {
        ASTFootballSite ast = parseModel("football/valid/MultiCountry.fb");
        Path outDir = tempDir.newFolder("brand-regression").toPath();
        new FootballSiteGenerator(outDir).generate(ast);

        // Should generate at least 7 HTML files
        // (1 index + 2 country + 4 league)
        long htmlFileCount = Files.walk(outDir)
                .filter(p -> p.toString().endsWith(".html"))
                .count();
        assertTrue("Should generate at least 7 HTML files, got " + htmlFileCount,
                htmlFileCount >= 7);

        // Every HTML file must contain GOODFELLAZßS at least twice
        // (header + footer) and must NOT contain old/wrong brand variants
        Files.walk(outDir)
                .filter(p -> p.toString().endsWith(".html"))
                .forEach(p -> {
                    try {
                        String content = readFile(p);
                        int brandCount = countOccurrences(content, "GOODFELLAZßS");
                        assertTrue("'" + p.getFileName()
                                + "' should contain 'GOODFELLAZßS' at least twice, found "
                                + brandCount,
                                brandCount >= 2);
                        assertFalse("'" + p.getFileName()
                                + "' should NOT contain old brand 'GOODFELLAZFß'",
                                content.contains("GOODFELLAZFß"));
                        assertFalse("'" + p.getFileName()
                                + "' should NOT contain typo 'GOODFELLAZSS'",
                                content.contains("GOODFELLAZSS"));
                    } catch (IOException e) {
                        fail("Could not read " + p + ": " + e.getMessage());
                    }
                });
    }
}
