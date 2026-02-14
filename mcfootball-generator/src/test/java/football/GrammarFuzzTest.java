package football;

import football.cocos.FootballSiteCoCos;
import football.footballsite._ast.ASTFootballSite;
import football.footballsite._cocos.FootballSiteCoCoChecker;
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
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Task 14 — Property-based / fuzzing tests for grammar robustness.
 *
 * Generates random but structurally valid .fb snippets and feeds
 * them through the parser + CoCos. The parser must never crash
 * (no uncaught exceptions); CoCos should either accept or cleanly
 * report errors.
 */
public class GrammarFuzzTest {

    private static final Random RNG = new Random(42); // reproducible seed

    private static final String[] TEAM_NAMES = {
        "FC Alpha", "SC Beta", "1. FC Köln", "Bayern München",
        "Córdoba CF", "São Paulo FC", "Łódź United", "Ñoño FC",
        "AC Zürich", "Malmö FF", "Fenerbahçe SK", "Étoile du Sahel",
        "Ålborg BK", "Brøndby IF", "Genève United"
    };

    private static final String[] CITY_NAMES = {
        "Berlin", "München", "Köln", "Düsseldorf", "Córdoba",
        "São Paulo", "Łódź", "Zürich", "Malmö", "Genève"
    };

    private static final String[] STADIUMS = {
        "Allianz Arena", "RheinEnergieStadion", "Estádio da Luz",
        "Stade Vélodrome", "Łódzki Stadion", "Malmö Stadion"
    };

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
    // Fuzz valid models: parser should never crash
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testFuzzValidModels_parserDoesNotCrash() throws IOException {
        for (int i = 0; i < 20; i++) {
            String model = generateValidModel(i);
            File tmpFile = File.createTempFile("fuzz_valid_" + i + "_", ".fb");
            tmpFile.deleteOnExit();
            Files.write(tmpFile.toPath(), model.getBytes(StandardCharsets.UTF_8));

            try {
                FootballSiteParser parser = new FootballSiteParser();
                Optional<ASTFootballSite> ast = parser.parseFootballSite(tmpFile.getAbsolutePath());
                assertTrue("Fuzz model " + i + " should parse", ast.isPresent());
            } catch (Exception e) {
                fail("Parser crashed on fuzz model " + i + ": " + e.getMessage()
                        + "\nModel:\n" + model);
            }
        }
    }

    @Test
    public void testFuzzValidModels_coCosDoNotCrash() throws IOException {
        for (int i = 0; i < 20; i++) {
            String model = generateValidModel(i);
            File tmpFile = File.createTempFile("fuzz_coco_" + i + "_", ".fb");
            tmpFile.deleteOnExit();
            Files.write(tmpFile.toPath(), model.getBytes(StandardCharsets.UTF_8));

            try {
                FootballSiteParser parser = new FootballSiteParser();
                Optional<ASTFootballSite> ast = parser.parseFootballSite(tmpFile.getAbsolutePath());
                if (ast.isPresent()) {
                    FootballSiteCoCoChecker checker = FootballSiteCoCos.createChecker();
                    checker.checkAll(ast.get());
                    // CoCos may report errors but must not throw
                }
            } catch (Exception e) {
                fail("CoCo check crashed on fuzz model " + i + ": " + e.getMessage()
                        + "\nModel:\n" + model);
            }

            Log.clearFindings();
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // Edge-case tests: boundary values
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testBoundaryScores() throws IOException {
        // Score 0-0 and 9-9
        String model = buildModel("BoundaryScore", "Testland", "TopLeague", "2025-2026",
                new String[][] {
                    { "2025-09-01", "15:00", "FC Alpha", "CityA", "SC Beta", "CityB", "0", "0", "Arena" },
                    { "2025-09-08", "18:30", "FC Alpha", "CityA", "SC Beta", "CityB", "9", "9", "Arena" }
                });

        File tmpFile = File.createTempFile("boundary_score_", ".fb");
        tmpFile.deleteOnExit();
        Files.write(tmpFile.toPath(), model.getBytes(StandardCharsets.UTF_8));

        FootballSiteParser parser = new FootballSiteParser();
        Optional<ASTFootballSite> ast = parser.parseFootballSite(tmpFile.getAbsolutePath());
        assertTrue("Boundary scores should parse", ast.isPresent());
    }

    @Test
    public void testBoundaryDates() throws IOException {
        // First and last day of year
        String model = buildModel("BoundaryDate", "Testland", "TopLeague", "2025-2026",
                new String[][] {
                    { "2025-01-01", "00:00", "FC Alpha", "CityA", "SC Beta", "CityB", "1", "0", "Arena" },
                    { "2026-12-31", "23:59", "FC Alpha", "CityA", "SC Beta", "CityB", "0", "1", "Arena" }
                });

        File tmpFile = File.createTempFile("boundary_date_", ".fb");
        tmpFile.deleteOnExit();
        Files.write(tmpFile.toPath(), model.getBytes(StandardCharsets.UTF_8));

        FootballSiteParser parser = new FootballSiteParser();
        Optional<ASTFootballSite> ast = parser.parseFootballSite(tmpFile.getAbsolutePath());
        assertTrue("Boundary dates should parse", ast.isPresent());
    }

    @Test
    public void testHeavyUnicodeInStrings() throws IOException {
        String model = buildModel("UnicodeHeavy", "Testland", "TopLeague", "2025-2026",
                new String[][] {
                    { "2025-09-01", "20:00",
                      "1. FC Köln", "Köln",
                      "Bayern München", "München",
                      "2", "1", "RheinEnergieStadion" },
                    { "2025-09-08", "21:00",
                      "Córdoba CF", "Córdoba",
                      "São Paulo FC", "São Paulo",
                      "0", "3", "Estádio da Luz" }
                });

        File tmpFile = File.createTempFile("unicode_heavy_", ".fb");
        tmpFile.deleteOnExit();
        Files.write(tmpFile.toPath(), model.getBytes(StandardCharsets.UTF_8));

        FootballSiteParser parser = new FootballSiteParser();
        Optional<ASTFootballSite> ast = parser.parseFootballSite(tmpFile.getAbsolutePath());
        assertTrue("Heavy Unicode model should parse", ast.isPresent());

        // CoCos should pass too
        long errorsBefore = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast.get());
        assertEquals("Heavy Unicode model should pass CoCos",
                errorsBefore, Log.getErrorCount());
    }

    // ══════════════════════════════════════════════════════════════════
    // Helpers: model builders
    // ══════════════════════════════════════════════════════════════════

    private String generateValidModel(int seed) {
        Random rng = new Random(seed);
        int matchCount = 1 + rng.nextInt(4);

        StringBuilder sb = new StringBuilder();
        sb.append("footballsite FuzzTest").append(seed).append(" {\n");
        sb.append("  navigation { Testland; }\n");
        sb.append("  country Testland {\n");
        sb.append("    league FuzzLeague season \"2025-2026\" {\n");

        for (int m = 0; m < matchCount; m++) {
            String home = TEAM_NAMES[rng.nextInt(TEAM_NAMES.length)];
            String away;
            do {
                away = TEAM_NAMES[rng.nextInt(TEAM_NAMES.length)];
            } while (away.equals(home));

            String city1 = CITY_NAMES[rng.nextInt(CITY_NAMES.length)];
            String city2 = CITY_NAMES[rng.nextInt(CITY_NAMES.length)];
            String stadium = STADIUMS[rng.nextInt(STADIUMS.length)];
            int month = 7 + rng.nextInt(6);  // Jul–Dec
            int day = 1 + rng.nextInt(28);
            int hour = rng.nextInt(24);
            int minute = rng.nextInt(4) * 15; // 0, 15, 30, 45
            int hs = rng.nextInt(6);
            int as = rng.nextInt(6);

            sb.append("      match {\n");
            sb.append(String.format("        date    \"2025-%02d-%02d\"\n", month, day));
            sb.append(String.format("        time    \"%02d:%02d\"\n", hour, minute));
            sb.append(String.format("        home    \"%s\"    (\"%s\")\n", home, city1));
            sb.append(String.format("        away    \"%s\"    (\"%s\")\n", away, city2));
            sb.append(String.format("        score   %d - %d\n", hs, as));
            sb.append(String.format("        stadium \"%s\"\n", stadium));
            sb.append("      }\n");
        }

        sb.append("    }\n");
        sb.append("  }\n");
        sb.append("}\n");
        return sb.toString();
    }

    private String buildModel(String siteName, String country, String league,
                               String season, String[][] matches) {
        StringBuilder sb = new StringBuilder();
        sb.append("footballsite ").append(siteName).append(" {\n");
        sb.append("  navigation { ").append(country).append("; }\n");
        sb.append("  country ").append(country).append(" {\n");
        sb.append("    league ").append(league).append(" season \"").append(season).append("\" {\n");

        for (String[] m : matches) {
            sb.append("      match {\n");
            sb.append("        date    \"").append(m[0]).append("\"\n");
            sb.append("        time    \"").append(m[1]).append("\"\n");
            sb.append("        home    \"").append(m[2]).append("\"    (\"").append(m[3]).append("\")\n");
            sb.append("        away    \"").append(m[4]).append("\"    (\"").append(m[5]).append("\")\n");
            sb.append("        score   ").append(m[6]).append(" - ").append(m[7]).append("\n");
            sb.append("        stadium \"").append(m[8]).append("\"\n");
            sb.append("      }\n");
        }

        sb.append("    }\n");
        sb.append("  }\n");
        sb.append("}\n");
        return sb.toString();
    }
}
