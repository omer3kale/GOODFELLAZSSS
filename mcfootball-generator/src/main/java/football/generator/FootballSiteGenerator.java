package football.generator;

/*
 * FootballSiteGenerator — static HTML generator for MCFootball.
 *
 * Walks the parsed ASTFootballSite and renders three kinds of pages
 * via FreeMarker templates found on the classpath under templates/:
 *
 *   index.ftl   → output/index.html
 *   country.ftl → output/<country-slug>/index.html
 *   league.ftl  → output/<country-slug>/<league-slug>/index.html
 *
 * Data is passed to templates as plain Map<String, Object> models
 * so FreeMarker stays simple (no AST-aware directives needed).
 *
 * Pattern: follows SLE-lite WebsiteGenerator.
 */

import football.footballsite._ast.ASTFootballSite;
import football.footballsite._ast.ASTCountry;
import football.footballsite._ast.ASTLeague;
import football.footballsite._ast.ASTMatch;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FootballSiteGenerator {

    /** Root directory for all generated HTML. */
    private final Path outputDir;

    /** FreeMarker configuration — loads templates from classpath. */
    private final Configuration cfg;

    // ──────────────────────────────────────────────────────────────────
    // Constructor
    // ──────────────────────────────────────────────────────────────────

    /**
     * @param outputDir root output directory for generated HTML
     */
    public FootballSiteGenerator(Path outputDir) {
        this.outputDir = outputDir;

        // FreeMarker setup — load templates from classpath (templates/)
        cfg = new Configuration(Configuration.VERSION_2_3_32);
        cfg.setClassLoaderForTemplateLoading(
                getClass().getClassLoader(), "templates");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(
                TemplateExceptionHandler.RETHROW_HANDLER);
    }

    // ──────────────────────────────────────────────────────────────────
    // Public entry point
    // ──────────────────────────────────────────────────────────────────

    /**
     * Generate the full static site from a parsed AST.
     *
     * 1. Build a shared "all countries with their leagues" list
     *    (used for navigation on every page).
     * 2. Render index page.
     * 3. For each country → country page, and for each league → league page.
     */
    public void generate(ASTFootballSite ast) {
        String siteName = ast.getName();
        List<Map<String, Object>> navCountries = buildNavData(ast);

        generateIndex(siteName, navCountries);

        for (ASTCountry country : ast.getCountryList()) {
            generateCountryPage(siteName, country, navCountries);

            for (ASTLeague league : country.getLeagueList()) {
                generateLeaguePage(siteName, country, league, navCountries);
            }
        }

        System.out.println("  Site generated: " + outputDir);
    }

    // ──────────────────────────────────────────────────────────────────
    // Per-page generators
    // ──────────────────────────────────────────────────────────────────

    /**
     * Render output/index.html from index.ftl.
     */
    protected void generateIndex(String siteName,
                                  List<Map<String, Object>> navCountries) {
        Map<String, Object> model = new HashMap<>();
        model.put("siteName", siteName);
        model.put("countries", navCountries);

        render("index.ftl", outputDir.resolve("index.html"), model);
    }

    /**
     * Render output/{country-slug}/index.html from country.ftl.
     */
    protected void generateCountryPage(String siteName,
                                        ASTCountry country,
                                        List<Map<String, Object>> navCountries) {
        String slug = toSlug(country.getName());

        // Build league list for this country
        List<Map<String, Object>> leagues = new ArrayList<>();
        for (ASTLeague league : country.getLeagueList()) {
            Map<String, Object> lm = new LinkedHashMap<>();
            lm.put("name", league.getName());
            lm.put("slug", toSlug(league.getName()));
            lm.put("season", league.getSeason());
            leagues.add(lm);
        }

        Map<String, Object> model = new HashMap<>();
        model.put("siteName", siteName);
        model.put("countryName", country.getName());
        model.put("countrySlug", slug);
        model.put("leagues", leagues);
        model.put("countries", navCountries);

        render("country.ftl",
               outputDir.resolve(slug).resolve("index.html"),
               model);
    }

    /**
     * Render output/{country-slug}/{league-slug}/index.html from league.ftl.
     */
    protected void generateLeaguePage(String siteName,
                                       ASTCountry country,
                                       ASTLeague league,
                                       List<Map<String, Object>> navCountries) {
        String countrySlug = toSlug(country.getName());
        String leagueSlug  = toSlug(league.getName());

        // Build match list as simple maps
        List<Map<String, Object>> matches = new ArrayList<>();
        for (ASTMatch m : league.getMatchList()) {
            Map<String, Object> mm = new LinkedHashMap<>();
            mm.put("date",     m.getMatchDate());
            mm.put("time",     m.getMatchTime());
            mm.put("homeTeam", m.getHomeTeam());
            mm.put("homeCity", m.getHomeCity());
            mm.put("awayTeam", m.getAwayTeam());
            mm.put("awayCity", m.getAwayCity());
            mm.put("homeScore", m.getHomeScore().getValue());
            mm.put("awayScore", m.getAwayScore().getValue());
            mm.put("stadium",  m.getStadium());
            matches.add(mm);
        }

        Map<String, Object> model = new HashMap<>();
        model.put("siteName",    siteName);
        model.put("countryName", country.getName());
        model.put("countrySlug", countrySlug);
        model.put("leagueName",  league.getName());
        model.put("leagueSlug",  leagueSlug);
        model.put("season",      league.getSeason());
        model.put("matches",     matches);
        model.put("countries",   navCountries);

        render("league.ftl",
               outputDir.resolve(countrySlug)
                        .resolve(leagueSlug)
                        .resolve("index.html"),
               model);
    }

    // ──────────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────────

    /**
     * Build a navigation data structure from the AST:
     * List of { name, slug, leagues: [ { name, slug, season } ] }
     */
    private List<Map<String, Object>> buildNavData(ASTFootballSite ast) {
        List<Map<String, Object>> countries = new ArrayList<>();
        for (ASTCountry c : ast.getCountryList()) {
            Map<String, Object> cm = new LinkedHashMap<>();
            cm.put("name", c.getName());
            cm.put("slug", toSlug(c.getName()));

            List<Map<String, Object>> leagues = new ArrayList<>();
            for (ASTLeague l : c.getLeagueList()) {
                Map<String, Object> lm = new LinkedHashMap<>();
                lm.put("name", l.getName());
                lm.put("slug", toSlug(l.getName()));
                lm.put("season", l.getSeason());
                leagues.add(lm);
            }
            cm.put("leagues", leagues);
            countries.add(cm);
        }
        return countries;
    }

    /**
     * Render a FreeMarker template to a file, creating parent dirs as needed.
     */
    private void render(String templateName, Path outputFile,
                        Map<String, Object> model) {
        try {
            File file = outputFile.toFile();
            file.getParentFile().mkdirs();

            Template template = cfg.getTemplate(templateName);
            try (Writer out = new FileWriter(file)) {
                template.process(model, out);
            }
            System.out.println("  wrote: " + outputFile);
        } catch (Exception e) {
            System.err.println("ERROR rendering " + templateName
                    + " → " + outputFile + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Convert a CamelCase or plain name to a URL-safe slug.
     *   "PremierLeague" → "premier-league"
     *   "Germany"       → "germany"
     *   "LaLiga"        → "la-liga"
     */
    public static String toSlug(String name) {
        // Insert '-' before each uppercase letter that follows a lowercase letter
        String slug = name.replaceAll("([a-z])([A-Z])", "$1-$2");
        return slug.toLowerCase();
    }
}
