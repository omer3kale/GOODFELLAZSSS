package football.backend.domain;

import java.util.List;

/**
 * A country containing one or more leagues — internal domain model.
 * Maps to the Country nonterminal in FootballSite.mc4.
 */
public class Country {

    private final String name;       // e.g. "Germany" (single word, CamelCase)
    private final List<League> leagues;

    public Country(String name, List<League> leagues) {
        this.name = name;
        this.leagues = leagues;
    }

    public String getName()           { return name; }
    public List<League> getLeagues()  { return leagues; }
}
