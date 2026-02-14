package football.backend.service;

/**
 * Summary DTO returned after a refresh operation.
 */
public class RefreshSummary {

    private final int countriesUpdated;
    private final int totalMatches;

    public RefreshSummary(int countriesUpdated, int totalMatches) {
        this.countriesUpdated = countriesUpdated;
        this.totalMatches = totalMatches;
    }

    public int getCountriesUpdated() { return countriesUpdated; }
    public int getTotalMatches()     { return totalMatches; }
}
