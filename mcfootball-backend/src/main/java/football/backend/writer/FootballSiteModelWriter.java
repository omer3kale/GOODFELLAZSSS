package football.backend.writer;

import football.backend.domain.Country;
import football.backend.domain.League;
import football.backend.domain.Match;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Serializes domain objects into syntactically valid .fb files
 * matching the FootballSite.mc4 grammar.
 * <p>
 * Output format example:
 * <pre>
 * footballsite GermanyResults {
 *   navigation { Germany; England; }
 *   country Germany {
 *     league Bundesliga season "2025-2026" {
 *       match {
 *         date    "2026-02-15"
 *         time    "15:30"
 *         home    "Bayern München"    ("Munich")
 *         away    "Borussia Dortmund" ("Dortmund")
 *         score   2 - 1
 *         stadium "Allianz Arena"
 *       }
 *     }
 *   }
 * }
 * </pre>
 */
@Component
public class FootballSiteModelWriter {

    /**
     * Build the .fb model string for a combined AllEurope footballsite
     * without writing to disk. Used by {@link football.backend.validation.ModelValidator}
     * to validate in-memory before committing to a file.
     *
     * @param countries all countries with their leagues and matches
     * @return the full .fb model string
     */
    public String toAllEuropeString(List<Country> countries) {
        String siteName = "AllEuropeResults";
        StringBuilder sb = new StringBuilder();

        sb.append("footballsite ").append(siteName).append(" {\n");
        sb.append("\n");

        sb.append("  navigation {\n");
        for (Country c : countries) {
            sb.append("    ").append(c.getName()).append(";\n");
        }
        sb.append("  }\n");
        sb.append("\n");

        for (Country country : countries) {
            appendCountryBlock(sb, country, "  ");
            sb.append("\n");
        }

        sb.append("}\n");
        return sb.toString();
    }

    /**
     * Write a single AllEurope.fb containing all countries in one footballsite block.
     * This is the production path — one parse, one AST, full site generation.
     *
     * @param outputDir  directory to write into (e.g. models/generated/)
     * @param countries  all countries with their leagues and matches
     * @throws IOException if file writing fails
     */
    public void writeAllEuropeModel(Path outputDir, List<Country> countries) throws IOException {
        Files.createDirectories(outputDir);

        Path filePath = outputDir.resolve("AllEurope.fb");
        String content = toAllEuropeString(countries);

        Files.write(filePath, content.getBytes(StandardCharsets.UTF_8));
        System.out.println("Wrote combined model: " + filePath);
    }

    /**
     * Write one .fb file per country (existing per-country behavior).
     * Retained for debugging — lets you inspect each country's model in isolation.
     *
     * @param outputDir  directory to write into
     * @param countries  all countries
     * @throws IOException if file writing fails
     */
    public void writeCountryModels(Path outputDir, List<Country> countries) throws IOException {
        List<String> allCountryNames = new java.util.ArrayList<>();
        for (Country c : countries) {
            allCountryNames.add(c.getName());
        }
        for (Country country : countries) {
            writeCountryModel(outputDir, allCountryNames, country);
        }
    }

    /**
     * Write a single .fb file for the given country.
     *
     * @param outputDir       directory to write into (e.g. models/generated/)
     * @param allCountryNames all country names for the navigation block
     * @param country         the country to serialize
     * @throws IOException if file writing fails
     */
    public void writeCountryModel(Path outputDir,
                                  List<String> allCountryNames,
                                  Country country) throws IOException {
        // Ensure output directory exists
        Files.createDirectories(outputDir);

        // Site name = "{CountryName}Results"
        String siteName = country.getName() + "Results";
        String fileName = country.getName() + ".fb";
        Path filePath = outputDir.resolve(fileName);

        StringBuilder sb = new StringBuilder();

        // ── Root: footballsite ───────────────────────────────────────
        sb.append("footballsite ").append(siteName).append(" {\n");
        sb.append("\n");

        // ── Navigation block ─────────────────────────────────────────
        sb.append("  navigation {\n");
        for (int i = 0; i < allCountryNames.size(); i++) {
            sb.append("    ").append(allCountryNames.get(i)).append(";");
            sb.append("\n");
        }
        sb.append("  }\n");
        sb.append("\n");

        // ── Country block ────────────────────────────────────────────
        appendCountryBlock(sb, country, "  ");

        sb.append("}\n");

        // Write file
        Files.write(filePath, sb.toString().getBytes(StandardCharsets.UTF_8));
        System.out.println("Wrote model: " + filePath);
    }

    /**
     * Append a single country block (with its leagues and matches) to the builder.
     */
    private void appendCountryBlock(StringBuilder sb, Country country, String indent) {
        sb.append(indent).append("country ").append(country.getName()).append(" {\n");

        for (League league : country.getLeagues()) {
            sb.append(indent).append("  league ").append(league.getName())
              .append(" season \"").append(league.getSeason())
              .append("\" {\n");

            for (Match match : league.getMatches()) {
                sb.append(indent).append("    match {\n");
                sb.append(indent).append("      date    \"").append(match.getDate()).append("\"\n");
                sb.append(indent).append("      time    \"").append(match.getTime()).append("\"\n");
                sb.append(indent).append("      home    \"").append(match.getHomeTeam())
                  .append("\"    (\"").append(match.getHomeCity()).append("\")\n");
                sb.append(indent).append("      away    \"").append(match.getAwayTeam())
                  .append("\"    (\"").append(match.getAwayCity()).append("\")\n");
                sb.append(indent).append("      score   ").append(match.getHomeScore())
                  .append(" - ").append(match.getAwayScore()).append("\n");
                sb.append(indent).append("      stadium \"").append(match.getStadium()).append("\"\n");
                sb.append(indent).append("    }\n");
            }

            sb.append(indent).append("  }\n");
        }

        sb.append(indent).append("}\n");
    }
}
