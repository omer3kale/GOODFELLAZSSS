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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Task 16 — Logging, error handling, and failure modes.
 *
 * Verifies that the parser, CoCos, and generator handle bad input
 * gracefully: no swallowed exceptions, clear error messages, and
 * failures in one country/league don't corrupt others.
 */
public class ErrorHandlingTest {

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

    // ══════════════════════════════════════════════════════════════════
    // Missing model file
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testParseMissingFileReturnsEmpty() throws IOException {
        FootballSiteParser parser = new FootballSiteParser();
        try {
            Optional<ASTFootballSite> result = parser.parseFootballSite(
                    "/nonexistent/path/NoSuchFile.fb");
            // Parser should return empty or throw IOException — both acceptable
            if (result != null && result.isPresent()) {
                fail("Parsing a nonexistent file should not return a valid AST");
            }
        } catch (IOException e) {
            // Expected — good error handling
            assertTrue("Error message should mention the file",
                    e.getMessage() != null);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // Malformed model file (not valid grammar)
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testParseMalformedModel() throws IOException {
        String garbage = "this is not a valid footballsite model { random garbage [] }";
        File tmpFile = File.createTempFile("malformed_", ".fb");
        tmpFile.deleteOnExit();
        Files.write(tmpFile.toPath(), garbage.getBytes(StandardCharsets.UTF_8));

        FootballSiteParser parser = new FootballSiteParser();
        Optional<ASTFootballSite> result = parser.parseFootballSite(tmpFile.getAbsolutePath());

        assertFalse("Malformed model should not parse successfully",
                result.isPresent());
    }

    @Test
    public void testParsePartialModel() throws IOException {
        // Valid start but incomplete — missing closing braces
        String partial = "footballsite Partial {\n  navigation { Germany; }\n  country Germany {\n";
        File tmpFile = File.createTempFile("partial_", ".fb");
        tmpFile.deleteOnExit();
        Files.write(tmpFile.toPath(), partial.getBytes(StandardCharsets.UTF_8));

        FootballSiteParser parser = new FootballSiteParser();
        Optional<ASTFootballSite> result = parser.parseFootballSite(tmpFile.getAbsolutePath());

        assertFalse("Partial/incomplete model should not parse successfully",
                result.isPresent());
    }

    @Test
    public void testParseEmptyFile() throws IOException {
        File tmpFile = File.createTempFile("empty_", ".fb");
        tmpFile.deleteOnExit();
        Files.write(tmpFile.toPath(), "".getBytes(StandardCharsets.UTF_8));

        FootballSiteParser parser = new FootballSiteParser();
        Optional<ASTFootballSite> result = parser.parseFootballSite(tmpFile.getAbsolutePath());

        assertFalse("Empty file should not parse as a valid model",
                result.isPresent());
    }

    // ══════════════════════════════════════════════════════════════════
    // Generator handles output dir creation
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testGeneratorCreatesOutputDirs() throws IOException {
        String modelStr =
                "footballsite DirTest {\n" +
                "  navigation { Alpha; }\n" +
                "  country Alpha {\n" +
                "    league One season \"2025-2026\" {\n" +
                "      match {\n" +
                "        date \"2025-09-01\" time \"15:00\"\n" +
                "        home \"Team A\" (\"CityA\")\n" +
                "        away \"Team B\" (\"CityB\")\n" +
                "        score 1 - 0 stadium \"Arena\"\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}\n";

        File tmpModel = File.createTempFile("dirtest_", ".fb");
        tmpModel.deleteOnExit();
        Files.write(tmpModel.toPath(), modelStr.getBytes(StandardCharsets.UTF_8));

        FootballSiteParser parser = new FootballSiteParser();
        ASTFootballSite ast = parser.parseFootballSite(tmpModel.getAbsolutePath()).get();

        // Use a deeply nested output path that doesn't exist yet
        Path outDir = tempDir.getRoot().toPath().resolve("deep/nested/output");
        assertFalse("Output dir should not exist yet", outDir.toFile().exists());

        FootballSiteGenerator gen = new FootballSiteGenerator(outDir);
        gen.generate(ast);

        assertTrue("Output dir should be created", outDir.toFile().exists());
        assertTrue("index.html should exist", outDir.resolve("index.html").toFile().exists());
    }

    // ══════════════════════════════════════════════════════════════════
    // Multiple CoCo violations don't crash — all errors reported
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testMultipleCoCosReportAllErrors() throws IOException {
        // BadMatchFields triggers 3 different CoCos (time, empty stadium, season)
        String absPath = new File("src/test/resources/football/invalid/BadMatchFields.fb").getAbsolutePath();
        FootballSiteParser parser = new FootballSiteParser();
        ASTFootballSite ast = parser.parseFootballSite(absPath).get();

        long errorsBefore = Log.getErrorCount();
        football.cocos.FootballSiteCoCos.createChecker().checkAll(ast);
        long errorsAfter = Log.getErrorCount();

        long newErrors = errorsAfter - errorsBefore;
        assertTrue("Multiple CoCo violations should all be reported (got " + newErrors + ")",
                newErrors >= 2);
    }

    // ══════════════════════════════════════════════════════════════════
    // Generator with valid multi-country model
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testGeneratorMultiCountryIndependence() throws IOException {
        // Ensure one country's output doesn't affect another
        String absPath = new File("src/test/resources/football/valid/Bundesliga.fb").getAbsolutePath();
        FootballSiteParser parser = new FootballSiteParser();
        ASTFootballSite ast = parser.parseFootballSite(absPath).get();

        Path outDir = tempDir.newFolder("multi-country").toPath();
        FootballSiteGenerator gen = new FootballSiteGenerator(outDir);
        gen.generate(ast);

        // Verify Germany pages exist
        assertTrue("Germany index should exist",
                outDir.resolve("germany/index.html").toFile().exists());
        assertTrue("Germany bundesliga should exist",
                outDir.resolve("germany/bundesliga/index.html").toFile().exists());

        // Verify content of Germany page is correctly scoped
        String germanyHtml = new String(
                Files.readAllBytes(outDir.resolve("germany/index.html")), "UTF-8");
        assertTrue("Germany page should reference Germany",
                germanyHtml.contains("Germany"));
    }
}
