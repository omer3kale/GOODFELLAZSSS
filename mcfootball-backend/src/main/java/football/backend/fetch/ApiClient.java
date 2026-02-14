package football.backend.fetch;

import java.util.List;

/**
 * Abstraction over any external football data vendor.
 * <p>
 * Implementations fetch raw fixture data for a given league and season.
 * Errors (HTTP, rate limit, timeout, parse) are reported via {@link ApiException}.
 */
public interface ApiClient {

    /**
     * Fetch fixtures for a specific league in a specific season.
     *
     * @param countryCode ISO country code (e.g. "DE", "GB", "ES")
     * @param leagueId    vendor-specific league ID (e.g. "78" for Bundesliga)
     * @param season      season year string (e.g. "2025")
     * @return raw fixture data from the API
     * @throws ApiException on HTTP errors, rate limits, or unparseable responses
     */
    List<ApiFixture> fetchFixtures(String countryCode,
                                   String leagueId,
                                   String season) throws ApiException;
}
