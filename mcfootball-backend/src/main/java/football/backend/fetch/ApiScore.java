package football.backend.fetch;

/**
 * DTO for a match score returned by the external football API.
 * Null values indicate the match has not yet been played.
 */
public class ApiScore {

    private Integer home;
    private Integer away;

    public ApiScore() {}

    public ApiScore(Integer home, Integer away) {
        this.home = home;
        this.away = away;
    }

    public Integer getHome() { return home; }
    public void setHome(Integer home) { this.home = home; }

    public Integer getAway() { return away; }
    public void setAway(Integer away) { this.away = away; }
}
