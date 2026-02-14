package football.cocos;

import football.footballsite._cocos.FootballSiteCoCoChecker;

/**
 * Registry for all FootballSite context conditions.
 * Creates and configures a FootballSiteCoCoChecker with all CoCo rules.
 *
 * <pre>
 * 0xFC001  CountryNameIsUnique           — no duplicate country names
 * 0xFC002  LeagueNameStartUpperCase      — league name starts uppercase
 * 0xFC003  MatchHasTwoDifferentTeams     — home ≠ away team
 * 0xFC004  NavigationCountryExists       — nav items reference real countries
 * 0xFC005  NoDuplicateLeaguePerCountry   — no duplicate leagues per country
 * 0xFC006  MatchDateFormatIsValid        — date matches YYYY-MM-DD
 * 0xFC007  MatchTimeFormatIsValid        — time matches HH:MM
 * 0xFC008  MatchFieldsNotEmpty           — no empty team/city/stadium strings
 * 0xFC009  SeasonFormatIsValid           — season matches YYYY-YYYY
 * 0xFC010  NavigationNoDuplicates        — no duplicate nav items
 * 0xFC011  CountryNameStartUpperCase     — country name starts uppercase
 * 0xFC012  NavigationMatchesAllCountries — every country appears in nav
 * </pre>
 */
public class FootballSiteCoCos {

    /**
     * Create a fully configured CoCo checker with all twelve context conditions.
     *
     * @return a ready-to-use checker
     */
    public static FootballSiteCoCoChecker createChecker() {
        FootballSiteCoCoChecker checker = new FootballSiteCoCoChecker();

        // ── Phase 5 — original CoCos ─────────────────────────────────
        checker.addCoCo(new CountryNameIsUnique());          // 0xFC001
        checker.addCoCo(new LeagueNameStartUpperCase());     // 0xFC002
        checker.addCoCo(new MatchHasTwoDifferentTeams());    // 0xFC003
        checker.addCoCo(new NavigationCountryExists());      // 0xFC004
        checker.addCoCo(new NoDuplicateLeaguePerCountry());  // 0xFC005
        checker.addCoCo(new MatchDateFormatIsValid());       // 0xFC006

        // ── Phase 5b — extended CoCos ────────────────────────────────
        checker.addCoCo(new MatchTimeFormatIsValid());       // 0xFC007
        checker.addCoCo(new MatchFieldsNotEmpty());          // 0xFC008
        checker.addCoCo(new SeasonFormatIsValid());          // 0xFC009
        checker.addCoCo(new NavigationNoDuplicates());       // 0xFC010
        checker.addCoCo(new CountryNameStartUpperCase());    // 0xFC011
        checker.addCoCo(new NavigationMatchesAllCountries()); // 0xFC012

        return checker;
    }
}
