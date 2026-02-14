package football.backend.fetch;

/**
 * DTO for a single fixture returned by the external football API.
 * Contains date-time string, teams, score, and venue information.
 */
public class ApiFixture {

    private String date;          // e.g. "2026-02-15T15:30:00+00:00"
    private ApiTeam homeTeam;
    private ApiTeam awayTeam;
    private ApiScore score;       // null if match not yet played
    private ApiVenue venue;

    public ApiFixture() {}

    public ApiFixture(String date, ApiTeam homeTeam, ApiTeam awayTeam,
                      ApiScore score, ApiVenue venue) {
        this.date = date;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.score = score;
        this.venue = venue;
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public ApiTeam getHomeTeam() { return homeTeam; }
    public void setHomeTeam(ApiTeam homeTeam) { this.homeTeam = homeTeam; }

    public ApiTeam getAwayTeam() { return awayTeam; }
    public void setAwayTeam(ApiTeam awayTeam) { this.awayTeam = awayTeam; }

    public ApiScore getScore() { return score; }
    public void setScore(ApiScore score) { this.score = score; }

    public ApiVenue getVenue() { return venue; }
    public void setVenue(ApiVenue venue) { this.venue = venue; }
}
