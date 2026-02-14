package football.backend.fetch;

import football.backend.config.ApiFetchConfig;
import org.springframework.stereotype.Component;

import java.net.http.HttpClient;
import java.util.List;

/**
 * Football data API client using JDK 11's HttpClient.
 * <p>
 * Currently returns an empty list (stub). The real HTTP integration
 * will be wired in a future phase once the API vendor is finalized.
 */
@Component
public class HttpApiFootballClient implements ApiClient {

    private final HttpClient httpClient;
    private final ApiFetchConfig config;

    public HttpApiFootballClient(ApiFetchConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public List<ApiFixture> fetchFixtures(String countryCode,
                                          String leagueId,
                                          String season) throws ApiException {
        // dynamic data to be followed
        return List.of();
    }
}
