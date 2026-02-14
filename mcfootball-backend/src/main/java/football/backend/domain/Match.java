package football.backend.domain;

/**
 * A single football match — internal domain model.
 * Maps directly to the Match nonterminal in FootballSite.mc4.
 */
public class Match {

    private final String date;       // e.g. "2026-02-15"
    private final String time;       // e.g. "15:30"
    private final String homeTeam;   // e.g. "Bayern München"
    private final String homeCity;   // e.g. "Munich"
    private final String awayTeam;   // e.g. "Borussia Dortmund"
    private final String awayCity;   // e.g. "Dortmund"
    private final int homeScore;
    private final int awayScore;
    private final String stadium;    // e.g. "Allianz Arena"

    public Match(String date, String time,
                 String homeTeam, String homeCity,
                 String awayTeam, String awayCity,
                 int homeScore, int awayScore,
                 String stadium) {
        this.date = date;
        this.time = time;
        this.homeTeam = homeTeam;
        this.homeCity = homeCity;
        this.awayTeam = awayTeam;
        this.awayCity = awayCity;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.stadium = stadium;
    }

    public String getDate()      { return date; }
    public String getTime()      { return time; }
    public String getHomeTeam()  { return homeTeam; }
    public String getHomeCity()  { return homeCity; }
    public String getAwayTeam()  { return awayTeam; }
    public String getAwayCity()  { return awayCity; }
    public int getHomeScore()    { return homeScore; }
    public int getAwayScore()    { return awayScore; }
    public String getStadium()   { return stadium; }
}
