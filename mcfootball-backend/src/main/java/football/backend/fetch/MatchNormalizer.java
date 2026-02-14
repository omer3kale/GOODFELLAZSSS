package football.backend.fetch;

import football.backend.domain.Match;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Converts raw {@link ApiFixture} DTOs from the external API into
 * domain {@link Match} objects suitable for .fb model generation.
 * <p>
 * Filtering rules:
 * <ul>
 *   <li>Fixtures with null scores are skipped (match not yet played).</li>
 *   <li>Fixtures with missing team names are skipped.</li>
 * </ul>
 * <p>
 * Date/time extraction:
 * <ul>
 *   <li>Date is the first 10 characters of the ISO-8601 string ("2026-02-15").</li>
 *   <li>Time is characters 11-16 ("15:30") if present, otherwise "00:00".</li>
 * </ul>
 */
@Component
public class MatchNormalizer {

    /**
     * Convert a list of API fixtures to domain matches, filtering invalid entries.
     *
     * @param fixtures raw API fixtures
     * @return valid domain matches
     */
    public List<Match> normalize(List<ApiFixture> fixtures) {
        return fixtures.stream()
                .filter(this::isValid)
                .map(this::toMatch)
                .collect(Collectors.toList());
    }

    private boolean isValid(ApiFixture f) {
        if (f.getScore() == null) return false;
        if (f.getScore().getHome() == null || f.getScore().getAway() == null) return false;
        if (f.getHomeTeam() == null || f.getHomeTeam().getName() == null) return false;
        if (f.getAwayTeam() == null || f.getAwayTeam().getName() == null) return false;
        if (f.getDate() == null || f.getDate().length() < 10) return false;
        return true;
    }

    private Match toMatch(ApiFixture f) {
        String date = f.getDate().substring(0, 10);                 // "2026-02-15"
        String time = f.getDate().length() >= 16
                ? f.getDate().substring(11, 16)                     // "15:30"
                : "00:00";

        String homeCity = f.getHomeTeam().getCity() != null
                ? f.getHomeTeam().getCity() : "";
        String awayCity = f.getAwayTeam().getCity() != null
                ? f.getAwayTeam().getCity() : "";
        String stadium  = (f.getVenue() != null && f.getVenue().getName() != null)
                ? f.getVenue().getName() : "";

        return new Match(
                date, time,
                f.getHomeTeam().getName(), homeCity,
                f.getAwayTeam().getName(), awayCity,
                f.getScore().getHome(), f.getScore().getAway(),
                stadium
        );
    }
}
