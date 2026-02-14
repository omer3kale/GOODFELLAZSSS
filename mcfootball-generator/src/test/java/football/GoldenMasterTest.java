package football;

import football.footballsite._ast.ASTFootballSite;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Task 13 — Golden-master / snapshot tests for HTML output.
 *
 * Generates HTML from a known model and compares key structural
 * elements against expectations. Acts as a regression guard for
 * template changes, branding, and navigation structure.
 *
 * If golden files need updating after an intentional change,
 * regenerate them via:
 *   ./gradlew :mcfootball-generator:generateSiteProd
 */
public class GoldenMasterTest {

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

    private ASTFootballSite parseModel(String resourcePath) throws IOException {
        String absPath = new File("src/test/resources/" + resourcePath).getAbsolutePath();
        FootballSiteParser parser = new FootballSiteParser();
        Optional<ASTFootballSite> opt = parser.parseFootballSite(absPath);
        assertTrue("Model should parse: " + resourcePath, opt.isPresent());
        return opt.get();
    }

    // ══════════════════════════════════════════════════════════════════
    // Structural snapshot: TinyTest model
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testTinyTestIndexStructure() throws IOException {
        ASTFootballSite ast = parseModel("football/valid/TinyTest.fb");
        Path outDir = tempDir.newFolder("golden-tiny").toPath();
        new FootballSiteGenerator(outDir).generate(ast);

        String html = readFile(outDir.resolve("index.html"));

        // Brand appears in title, header, footer
        assertContainsCount(html, "GOODFELLAZßS", 3);

        // DOCTYPE and charset
        assertTrue("Should have DOCTYPE", html.contains("<!DOCTYPE html>"));
        assertTrue("Should have UTF-8 charset", html.contains("charset=\"UTF-8\""));

        // Navigation link to country
        assertTrue("Should link to testland",
                html.contains("testland/index.html"));

        // White background
        assertTrue("Should have white body bg",
                html.contains("background-color: #ffffff"));

        // Black header/footer
        assertTrue("Should have black header/footer bg",
                html.contains("background-color: #000000"));
    }

    @Test
    public void testTinyTestCountryStructure() throws IOException {
        ASTFootballSite ast = parseModel("football/valid/TinyTest.fb");
        Path outDir = tempDir.newFolder("golden-country").toPath();
        new FootballSiteGenerator(outDir).generate(ast);

        String html = readFile(outDir.resolve("testland/index.html"));

        // Brand in header + footer
        assertContainsCount(html, "GOODFELLAZßS", 3);

        // Country name in title
        assertTrue("Title should contain Testland",
                html.contains("Testland"));

        // Link to league
        assertTrue("Should link to top-league",
                html.contains("top-league/index.html"));

        // Link back to home
        assertTrue("Should link back to index",
                html.contains("../index.html"));
    }

    @Test
    public void testTinyTestLeagueStructure() throws IOException {
        ASTFootballSite ast = parseModel("football/valid/TinyTest.fb");
        Path outDir = tempDir.newFolder("golden-league").toPath();
        new FootballSiteGenerator(outDir).generate(ast);

        String html = readFile(outDir.resolve("testland/top-league/index.html"));

        // Brand in title + header + footer
        assertContainsCount(html, "GOODFELLAZßS", 3);

        // Team names with correct CSS class
        assertTrue("Should contain team-name class",
                html.contains("class=\"team-name\""));

        // Score with correct CSS class
        assertTrue("Should contain score class",
                html.contains("class=\"score\""));

        // Match data present
        assertTrue("Should contain FC Alpha", html.contains("FC Alpha"));
        assertTrue("Should contain SC Beta", html.contains("SC Beta"));
        assertTrue("Should contain Alpha Arena", html.contains("Alpha Arena"));

        // Breadcrumb: link back to country and home
        assertTrue("Should link to home (../../index.html)",
                html.contains("../../index.html"));

        // Season text
        assertTrue("Should contain season 2025-2026",
                html.contains("2025-2026"));
    }

    @Test
    public void testBundesligaMatchCount() throws IOException {
        ASTFootballSite ast = parseModel("football/valid/Bundesliga.fb");
        Path outDir = tempDir.newFolder("golden-buli").toPath();
        new FootballSiteGenerator(outDir).generate(ast);

        String html = readFile(outDir.resolve("germany/bundesliga/index.html"));

        // Should have 2 matches rendered
        int matchRows = countOccurrences(html, "class=\"team-name\"");
        // Each match has 2 team-name spans (home + away)
        assertEquals("Bundesliga should have 2 matches (4 team-name spans)", 4, matchRows);
    }

    @Test
    public void testNoLeakedOldBrand() throws IOException {
        ASTFootballSite ast = parseModel("football/valid/TinyTest.fb");
        Path outDir = tempDir.newFolder("golden-nobrand").toPath();
        new FootballSiteGenerator(outDir).generate(ast);

        // Walk all generated HTML and ensure old brand does NOT appear
        Files.walk(outDir).filter(p -> p.toString().endsWith(".html")).forEach(p -> {
            try {
                String content = readFile(p);
                assertFalse("Should NOT contain old brand GOODFELLAZFß in " + p,
                        content.contains("GOODFELLAZFß"));
            } catch (IOException e) {
                fail("Could not read " + p + ": " + e.getMessage());
            }
        });
    }

    // ══════════════════════════════════════════════════════════════════
    // Helpers
    // ══════════════════════════════════════════════════════════════════

    private String readFile(Path path) throws IOException {
        return new String(Files.readAllBytes(path), "UTF-8");
    }

    private void assertContainsCount(String text, String substring, int expectedMin) {
        int count = countOccurrences(text, substring);
        assertTrue("Expected at least " + expectedMin + " occurrences of '"
                + substring + "' but found " + count, count >= expectedMin);
    }

    private int countOccurrences(String text, String sub) {
        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf(sub, idx)) != -1) {
            count++;
            idx += sub.length();
        }
        return count;
    }
}
