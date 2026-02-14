package football.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.util.List;

/**
 * Externalized configuration for API-based football data fetching.
 * <p>
 * Bound from {@code mcfootball.api.*} in application.yml.
 * Each country has a list of leagues; each league has a vendor-specific ID.
 *
 * <pre>
 * mcfootball:
 *   api:
 *     season: "2025"
 *     min-success-rate: 0.5
 *     countries:
 *       - name: Germany
 *         code: DE
 *         leagues:
 *           - name: Bundesliga
 *             id: "78"
 * </pre>
 */
@ConfigurationProperties(prefix = "mcfootball.api")
@ConstructorBinding
public class ApiFetchConfig {

    private final String season;
    private final double minSuccessRate;
    private final List<CountrySpec> countries;

    public ApiFetchConfig(String season, double minSuccessRate,
                          List<CountrySpec> countries) {
        this.season = season;
        this.minSuccessRate = minSuccessRate;
        this.countries = countries;
    }

    public String getSeason()           { return season; }
    public double getMinSuccessRate()   { return minSuccessRate; }
    public List<CountrySpec> getCountries() { return countries; }

    /**
     * Specification for a single country's data source.
     */
    public static class CountrySpec {

        private final String name;
        private final String code;
        private final List<LeagueSpec> leagues;

        @ConstructorBinding
        public CountrySpec(String name, String code, List<LeagueSpec> leagues) {
            this.name = name;
            this.code = code;
            this.leagues = leagues;
        }

        public String getName()              { return name; }
        public String getCode()              { return code; }
        public List<LeagueSpec> getLeagues() { return leagues; }
    }

    /**
     * Specification for a single league within a country.
     */
    public static class LeagueSpec {

        private final String name;
        private final String id;

        @ConstructorBinding
        public LeagueSpec(String name, String id) {
            this.name = name;
            this.id = id;
        }

        public String getName() { return name; }
        public String getId()   { return id; }
    }
}
