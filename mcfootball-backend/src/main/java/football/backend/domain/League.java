package football.backend.domain;

import java.util.List;

/**
 * A football league — internal domain model.
 * Maps to the League nonterminal in FootballSite.mc4.
 */
public class League {

    private final String name;       // e.g. "Bundesliga" (CamelCase, no spaces)
    private final String season;     // e.g. "2025-2026"
    private final List<Match> matches;

    public League(String name, String season, List<Match> matches) {
        this.name = name;
        this.season = season;
        this.matches = matches;
    }

    public String getName()          { return name; }
    public String getSeason()        { return season; }
    public List<Match> getMatches()  { return matches; }
}
