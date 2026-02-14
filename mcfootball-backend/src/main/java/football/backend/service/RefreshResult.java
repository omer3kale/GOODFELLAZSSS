package football.backend.service;

import java.util.List;

/**
 * Rich result DTO returned by the fetch → validate → write pipeline.
 * Replaces the simpler {@link RefreshSummary} for the production endpoint.
 */
public class RefreshResult {

    private final RefreshStatus outcome;
    private final int countriesRequested;
    private final int countriesSucceeded;
    private final int countriesFailed;
    private final int totalMatches;
    private final int cocoErrorCount;
    private final List<String> apiErrors;
    private final List<String> cocoErrors;
    private final boolean modelWritten;

    private RefreshResult(Builder b) {
        this.outcome            = b.outcome;
        this.countriesRequested = b.countriesRequested;
        this.countriesSucceeded = b.countriesSucceeded;
        this.countriesFailed    = b.countriesFailed;
        this.totalMatches       = b.totalMatches;
        this.cocoErrorCount     = b.cocoErrorCount;
        this.apiErrors          = b.apiErrors;
        this.cocoErrors         = b.cocoErrors;
        this.modelWritten       = b.modelWritten;
    }

    // ── Getters ────────────────────────────────────────────────────

    public RefreshStatus getOutcome()     { return outcome; }
    public int getCountriesRequested()    { return countriesRequested; }
    public int getCountriesSucceeded()    { return countriesSucceeded; }
    public int getCountriesFailed()       { return countriesFailed; }
    public int getTotalMatches()          { return totalMatches; }
    public int getCocoErrorCount()        { return cocoErrorCount; }
    public List<String> getApiErrors()    { return apiErrors; }
    public List<String> getCocoErrors()   { return cocoErrors; }
    public boolean isModelWritten()       { return modelWritten; }

    // ── Builder ────────────────────────────────────────────────────

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private RefreshStatus outcome = RefreshStatus.SUCCESS;
        private int countriesRequested;
        private int countriesSucceeded;
        private int countriesFailed;
        private int totalMatches;
        private int cocoErrorCount;
        private List<String> apiErrors = List.of();
        private List<String> cocoErrors = List.of();
        private boolean modelWritten;

        public Builder outcome(RefreshStatus s)         { this.outcome = s; return this; }
        public Builder countriesRequested(int n)        { this.countriesRequested = n; return this; }
        public Builder countriesSucceeded(int n)        { this.countriesSucceeded = n; return this; }
        public Builder countriesFailed(int n)           { this.countriesFailed = n; return this; }
        public Builder totalMatches(int n)              { this.totalMatches = n; return this; }
        public Builder cocoErrorCount(int n)            { this.cocoErrorCount = n; return this; }
        public Builder apiErrors(List<String> e)        { this.apiErrors = e; return this; }
        public Builder cocoErrors(List<String> e)       { this.cocoErrors = e; return this; }
        public Builder modelWritten(boolean b)          { this.modelWritten = b; return this; }

        public RefreshResult build() { return new RefreshResult(this); }
    }
}
