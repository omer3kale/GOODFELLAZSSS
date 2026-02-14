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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Task 12 — Unit tests for core Java components:
 *   FootballSiteTool, FootballSiteGenerator, FootballSiteCoCos.
 *
 * Tests valid parsing, CoCo positive/negative checks, and HTML generation.
 */
public class FootballSiteToolTest {

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

    // ── Helper: parse a model file from test resources ────────────────

    private Optional<ASTFootballSite> parseModel(String resourcePath) throws IOException {
        String absPath = new File("src/test/resources/" + resourcePath).getAbsolutePath();
        FootballSiteParser parser = new FootballSiteParser();
        return parser.parseFootballSite(absPath);
    }

    // ══════════════════════════════════════════════════════════════════
    // Task 12a — Valid models parse without errors
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testParseValidBundesliga() throws IOException {
        Optional<ASTFootballSite> ast = parseModel("football/valid/Bundesliga.fb");
        assertTrue("Bundesliga.fb should parse successfully", ast.isPresent());
        assertEquals("BundesligaResults", ast.get().getName());
        assertEquals(1, ast.get().getCountryList().size());
    }

    @Test
    public void testParseValidPremierLeague() throws IOException {
        Optional<ASTFootballSite> ast = parseModel("football/valid/PremierLeague.fb");
        assertTrue("PremierLeague.fb should parse successfully", ast.isPresent());
    }

    @Test
    public void testParseValidTinyTest() throws IOException {
        Optional<ASTFootballSite> ast = parseModel("football/valid/TinyTest.fb");
        assertTrue("TinyTest.fb should parse successfully", ast.isPresent());
        assertEquals("TinyTest", ast.get().getName());
        assertEquals(1, ast.get().getCountryList().size());
        assertEquals(1, ast.get().getCountryList().get(0).getLeagueList().size());
        assertEquals(2, ast.get().getCountryList().get(0).getLeagueList().get(0).getMatchList().size());
    }

    // ══════════════════════════════════════════════════════════════════
    // Task 12b — CoCos pass on valid models
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testCoCosPassOnValidBundesliga() throws IOException {
        ASTFootballSite ast = parseModel("football/valid/Bundesliga.fb").get();
        long errorsBefore = Log.getErrorCount();
        FootballSiteCoCoChecker checker = FootballSiteCoCos.createChecker();
        checker.checkAll(ast);
        assertEquals("CoCos should pass on valid Bundesliga model",
                errorsBefore, Log.getErrorCount());
    }

    @Test
    public void testCoCosPassOnValidTinyTest() throws IOException {
        ASTFootballSite ast = parseModel("football/valid/TinyTest.fb").get();
        long errorsBefore = Log.getErrorCount();
        FootballSiteCoCoChecker checker = FootballSiteCoCos.createChecker();
        checker.checkAll(ast);
        assertEquals("CoCos should pass on valid TinyTest model",
                errorsBefore, Log.getErrorCount());
    }

    // ══════════════════════════════════════════════════════════════════
    // Task 12c — CoCos detect violations on invalid models (negative tests)
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testCoCoDuplicateCountry() throws IOException {
        // Triggers 0xFC001 CountryNameIsUnique
        ASTFootballSite ast = parseModel("football/invalid/DuplicateCountry.fb").get();
        long errorsBefore = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertTrue("DuplicateCountry should trigger CoCo errors",
                Log.getErrorCount() > errorsBefore);
    }

    @Test
    public void testCoCoSameTeam() throws IOException {
        // Triggers 0xFC003 MatchHasTwoDifferentTeams + 0xFC006 MatchDateFormatIsValid
        ASTFootballSite ast = parseModel("football/invalid/SameTeamMatch.fb").get();
        long errorsBefore = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertTrue("SameTeamMatch should trigger CoCo errors",
                Log.getErrorCount() > errorsBefore);
    }

    @Test
    public void testCoCoBadNavigation() throws IOException {
        // Triggers 0xFC004 NavigationCountryExists + 0xFC002 LeagueNameStartUpperCase
        ASTFootballSite ast = parseModel("football/invalid/BadNavigation.fb").get();
        long errorsBefore = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertTrue("BadNavigation should trigger CoCo errors",
                Log.getErrorCount() > errorsBefore);
    }

    @Test
    public void testCoCoBadMatchFields() throws IOException {
        // Triggers 0xFC007, 0xFC008, 0xFC009
        ASTFootballSite ast = parseModel("football/invalid/BadMatchFields.fb").get();
        long errorsBefore = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertTrue("BadMatchFields should trigger CoCo errors",
                Log.getErrorCount() > errorsBefore);
    }

    @Test
    public void testCoCoNavIssues() throws IOException {
        // Triggers 0xFC010 NavigationNoDuplicates + 0xFC012 NavigationMatchesAllCountries
        ASTFootballSite ast = parseModel("football/invalid/NavIssues.fb").get();
        long errorsBefore = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertTrue("NavIssues should trigger CoCo errors",
                Log.getErrorCount() > errorsBefore);
    }

    @Test
    public void testCoCoBadDateTime() throws IOException {
        // Triggers 0xFC006 MatchDateFormatIsValid + 0xFC007 MatchTimeFormatIsValid
        ASTFootballSite ast = parseModel("football/invalid/BadDateTime.fb").get();
        long errorsBefore = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertTrue("BadDateTime should trigger CoCo errors",
                Log.getErrorCount() > errorsBefore);
    }

    @Test
    public void testCoCoBadSeason() throws IOException {
        // Triggers 0xFC009 SeasonFormatIsValid
        ASTFootballSite ast = parseModel("football/invalid/BadSeason.fb").get();
        long errorsBefore = Log.getErrorCount();
        FootballSiteCoCos.createChecker().checkAll(ast);
        assertTrue("BadSeason should trigger CoCo errors",
                Log.getErrorCount() > errorsBefore);
    }

    // ══════════════════════════════════════════════════════════════════
    // Task 12d — Generator produces HTML from valid model
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testGeneratorProducesHtml() throws IOException {
        ASTFootballSite ast = parseModel("football/valid/TinyTest.fb").get();
        Path outDir = tempDir.newFolder("site").toPath();
        FootballSiteGenerator gen = new FootballSiteGenerator(outDir);
        gen.generate(ast);

        // Check index page
        assertTrue("index.html should exist",
                outDir.resolve("index.html").toFile().exists());
        // Check country page
        assertTrue("testland/index.html should exist",
                outDir.resolve("testland/index.html").toFile().exists());
        // Check league page
        assertTrue("testland/top-league/index.html should exist",
                outDir.resolve("testland/top-league/index.html").toFile().exists());
    }

    @Test
    public void testGeneratorOutputContainsBrand() throws IOException {
        ASTFootballSite ast = parseModel("football/valid/TinyTest.fb").get();
        Path outDir = tempDir.newFolder("brand-site").toPath();
        FootballSiteGenerator gen = new FootballSiteGenerator(outDir);
        gen.generate(ast);

        String indexContent = new String(
                Files.readAllBytes(outDir.resolve("index.html")), "UTF-8");
        assertTrue("index.html should contain brand GOODFELLAZßS",
                indexContent.contains("GOODFELLAZßS"));
        assertTrue("index.html should contain charset UTF-8",
                indexContent.contains("charset=\"UTF-8\""));
    }

    @Test
    public void testGeneratorOutputContainsTeamData() throws IOException {
        ASTFootballSite ast = parseModel("football/valid/TinyTest.fb").get();
        Path outDir = tempDir.newFolder("data-site").toPath();
        FootballSiteGenerator gen = new FootballSiteGenerator(outDir);
        gen.generate(ast);

        String leagueContent = new String(
                Files.readAllBytes(outDir.resolve("testland/top-league/index.html")), "UTF-8");
        assertTrue("League page should contain home team",
                leagueContent.contains("FC Alpha"));
        assertTrue("League page should contain away team",
                leagueContent.contains("SC Beta"));
        assertTrue("League page should contain stadium",
                leagueContent.contains("Alpha Arena"));
    }

    // ══════════════════════════════════════════════════════════════════
    // Task 12e — toSlug utility tests
    // ══════════════════════════════════════════════════════════════════

    @Test
    public void testToSlugCamelCase() {
        assertEquals("premier-league", FootballSiteGenerator.toSlug("PremierLeague"));
    }

    @Test
    public void testToSlugSimple() {
        assertEquals("germany", FootballSiteGenerator.toSlug("Germany"));
    }

    @Test
    public void testToSlugMultiHump() {
        assertEquals("la-liga2", FootballSiteGenerator.toSlug("LaLiga2"));
    }

    @Test
    public void testToSlugSingleWord() {
        assertEquals("national", FootballSiteGenerator.toSlug("National"));
    }
}
