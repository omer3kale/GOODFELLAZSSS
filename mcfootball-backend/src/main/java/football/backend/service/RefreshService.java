package football.backend.service;

import football.backend.config.ApiFetchConfig;
import football.backend.domain.Country;
import football.backend.domain.League;
import football.backend.domain.Match;
import football.backend.fetch.ApiClient;
import football.backend.fetch.ApiException;
import football.backend.fetch.ApiFixture;
import football.backend.fetch.MatchNormalizer;
import football.backend.validation.ModelValidator;
import football.backend.validation.ValidationResult;
import football.backend.writer.FootballSiteModelWriter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Orchestrates data fetching and .fb model file generation.
 * <p>
 * Production pipeline (POST /refresh/all):
 *   fetch from API → normalize → validate with CoCos → write AllEurope.fb
 * <p>
 * Debug path (POST /refresh/all-debug):
 *   uses hard-coded data, writes per-country + AllEurope.fb
 * <p>
 * Legacy (POST /refresh/dummy):
 *   Germany-only hard-coded data
 * <p>
 * The {@link ApiClient} is currently stubbed (returns empty lists),
 * so the production path falls back to hard-coded data until
 * the real HTTP integration is wired.
 */
@Service
public class RefreshService {

    private final FootballSiteModelWriter modelWriter;
    private final ApiClient apiClient;
    private final MatchNormalizer normalizer;
    private final ApiFetchConfig config;
    private final ModelValidator validator;

    public RefreshService(FootballSiteModelWriter modelWriter,
                          ApiClient apiClient,
                          MatchNormalizer normalizer,
                          ApiFetchConfig config,
                          ModelValidator validator) {
        this.modelWriter = modelWriter;
        this.apiClient   = apiClient;
        this.normalizer  = normalizer;
        this.config      = config;
        this.validator   = validator;
    }

    // ── Production: fetch → validate → write ─────────────────────────

    /**
     * Full production pipeline:
     * 1. Fetch fixtures for each configured country/league via ApiClient
     * 2. Normalize API fixtures into domain matches
     * 3. Build Countries, generate .fb string
     * 4. Validate with MontiCore parser + CoCos
     * 5. Write AllEurope.fb only if validation passes
     *
     * Falls back to hard-coded data if the API returns nothing
     * (stub behaviour during development).
     *
     * @return rich result with outcome, error details, and counts
     */
    public RefreshResult refreshAllEurope() {
        List<Country> countries;
        List<String> apiErrors = new ArrayList<>();
        int leaguesRequested = 0;
        int leaguesSucceeded = 0;

        // ── Step 1: Fetch from API ───────────────────────────────────
        List<Country> fetched = fetchAllCountries(apiErrors);
        leaguesRequested = config.getCountries().stream()
                .mapToInt(c -> c.getLeagues().size())
                .sum();
        leaguesSucceeded = (int) fetched.stream()
                .flatMap(c -> c.getLeagues().stream())
                .filter(l -> !l.getMatches().isEmpty())
                .count();

        // If API returned nothing (stub), fall back to hard-coded data
        boolean anyMatches = fetched.stream()
                .flatMap(c -> c.getLeagues().stream())
                .anyMatch(l -> !l.getMatches().isEmpty());
        if (!anyMatches) {
            countries = buildAllCountries();
        } else {
            // Check success-rate threshold
            double rate = leaguesRequested > 0
                    ? (double) leaguesSucceeded / leaguesRequested
                    : 0.0;
            if (rate < config.getMinSuccessRate()) {
                int totalMatches = fetched.stream()
                        .flatMap(c -> c.getLeagues().stream())
                        .mapToInt(l -> l.getMatches().size())
                        .sum();
                return RefreshResult.builder()
                        .outcome(RefreshStatus.FETCH_BELOW_THRESHOLD)
                        .countriesRequested(config.getCountries().size())
                        .countriesSucceeded((int) fetched.stream()
                                .filter(c -> c.getLeagues().stream()
                                        .anyMatch(l -> !l.getMatches().isEmpty()))
                                .count())
                        .countriesFailed(config.getCountries().size() - leaguesSucceeded)
                        .totalMatches(totalMatches)
                        .apiErrors(apiErrors)
                        .modelWritten(false)
                        .build();
            }
            countries = fetched;
        }

        int totalMatches = countries.stream()
                .flatMap(c -> c.getLeagues().stream())
                .mapToInt(l -> l.getMatches().size())
                .sum();

        // ── Step 2: Validate with CoCos ──────────────────────────────
        String modelContent = modelWriter.toAllEuropeString(countries);
        ValidationResult vr = validator.validate(modelContent);

        if (!vr.isValid()) {
            return RefreshResult.builder()
                    .outcome(RefreshStatus.COCO_VALIDATION_FAILED)
                    .countriesRequested(config.getCountries().size())
                    .countriesSucceeded(countries.size())
                    .countriesFailed(0)
                    .totalMatches(totalMatches)
                    .cocoErrorCount(vr.getErrorCount())
                    .cocoErrors(vr.getErrors())
                    .apiErrors(apiErrors)
                    .modelWritten(false)
                    .build();
        }

        // ── Step 3: Write .fb model ──────────────────────────────────
        try {
            Path modelsDir = getModelsGeneratedDir();
            modelWriter.writeAllEuropeModel(modelsDir, countries);
        } catch (IOException e) {
            return RefreshResult.builder()
                    .outcome(RefreshStatus.WRITE_ERROR)
                    .countriesRequested(config.getCountries().size())
                    .countriesSucceeded(countries.size())
                    .countriesFailed(0)
                    .totalMatches(totalMatches)
                    .apiErrors(apiErrors)
                    .modelWritten(false)
                    .build();
        }

        return RefreshResult.builder()
                .outcome(RefreshStatus.SUCCESS)
                .countriesRequested(config.getCountries().size())
                .countriesSucceeded(countries.size())
                .countriesFailed(0)
                .totalMatches(totalMatches)
                .apiErrors(apiErrors)
                .modelWritten(true)
                .build();
    }

    // ── Debug: hard-coded data, per-country + combined ───────────────

    /**
     * Fetch + write per-country .fb files AND the combined AllEurope.fb.
     * Debug path — called by POST /refresh/all-debug.
     *
     * @return summary of what was written
     * @throws IOException if file writing fails
     */
    public RefreshSummary refreshAllEuropeDebug() throws IOException {
        List<Country> countries = buildAllCountries();
        Path modelsDir = getModelsGeneratedDir();
        // Per-country files for inspection
        modelWriter.writeCountryModels(modelsDir, countries);
        // Combined file for production generator
        modelWriter.writeAllEuropeModel(modelsDir, countries);

        int totalMatches = countries.stream()
                .flatMap(c -> c.getLeagues().stream())
                .mapToInt(l -> l.getMatches().size())
                .sum();
        return new RefreshSummary(countries.size(), totalMatches);
    }

    // ── Legacy: Germany-only ─────────────────────────────────────────

    /**
     * Write a Germany-only .fb file (backward compat).
     */
    public RefreshSummary refreshDummyGermany() throws IOException {
        Country germany = buildGermany();
        List<String> allCountries = Collections.singletonList("Germany");
        Path modelsDir = getModelsGeneratedDir();
        modelWriter.writeCountryModel(modelsDir, allCountries, germany);

        int totalMatches = germany.getLeagues().stream()
                .mapToInt(l -> l.getMatches().size())
                .sum();
        return new RefreshSummary(1, totalMatches);
    }

    // ── API fetch with per-league error handling ─────────────────────

    /**
     * Fetch fixtures for each configured country/league.
     * Errors per league are caught and accumulated, not propagated.
     *
     * @param apiErrors mutable list to collect error messages
     * @return countries with whatever data was successfully fetched
     */
    private List<Country> fetchAllCountries(List<String> apiErrors) {
        List<Country> result = new ArrayList<>();

        for (ApiFetchConfig.CountrySpec cs : config.getCountries()) {
            List<League> leagues = new ArrayList<>();

            for (ApiFetchConfig.LeagueSpec ls : cs.getLeagues()) {
                try {
                    List<ApiFixture> fixtures = apiClient.fetchFixtures(
                            cs.getCode(), ls.getId(), config.getSeason());
                    List<Match> matches = normalizer.normalize(fixtures);
                    leagues.add(new League(ls.getName(), config.getSeason(), matches));
                } catch (ApiException e) {
                    apiErrors.add(cs.getName() + "/" + ls.getName()
                            + ": " + e.getMessage());
                    // Add league with empty matches so it's counted as failed
                    leagues.add(new League(ls.getName(), config.getSeason(),
                            Collections.emptyList()));
                }
            }

            result.add(new Country(cs.getName(), leagues));
        }

        return result;
    }

    // ── Data builders (hard-coded fallback) ──────────────────────────

    private List<Country> buildAllCountries() {
        return Arrays.asList(buildGermany(), buildEngland(), buildSpain());
    }

    private Country buildGermany() {
        Match m1 = new Match(
                "2026-02-15", "15:30",
                "Bayern München", "Munich",
                "Borussia Dortmund", "Dortmund",
                2, 1, "Allianz Arena");
        League bundesliga = new League("Bundesliga", "2025-2026",
                Collections.singletonList(m1));
        return new Country("Germany", Collections.singletonList(bundesliga));
    }

    private Country buildEngland() {
        Match m1 = new Match(
                "2026-02-15", "15:00",
                "Arsenal", "London",
                "Manchester City", "Manchester",
                1, 1, "Emirates Stadium");
        League premierLeague = new League("PremierLeague", "2025-2026",
                Collections.singletonList(m1));
        return new Country("England", Collections.singletonList(premierLeague));
    }

    private Country buildSpain() {
        Match m1 = new Match(
                "2026-02-15", "21:00",
                "Real Madrid", "Madrid",
                "FC Barcelona", "Barcelona",
                3, 2, "Santiago Bernabéu");
        League laLiga = new League("LaLiga", "2025-2026",
                Collections.singletonList(m1));
        return new Country("Spain", Collections.singletonList(laLiga));
    }

    /**
     * Compute the shared models/generated/ directory path.
     */
    public static Path getModelsGeneratedDir() {
        Path candidate = Paths.get(System.getProperty("user.dir"),
                "models", "generated");

        if (System.getProperty("user.dir").endsWith("mcfootball-backend")) {
            candidate = Paths.get(System.getProperty("user.dir"))
                    .getParent()
                    .resolve("models")
                    .resolve("generated");
        }

        return candidate;
    }
}
