package football.cocos;

import football.footballsite._cocos.FootballSiteCoCoChecker;

/**
 * Registry for all FootballSite context conditions.
 * Creates and configures a FootballSiteCoCoChecker with all CoCo rules.
 *
 * <pre>
 * 0xFC001  CountryNameIsUnique                 — no duplicate country names
 * 0xFC002  LeagueNameStartUpperCase            — league name starts uppercase
 * 0xFC003  MatchHasTwoDifferentTeams           — home ≠ away team
 * 0xFC004  NavigationCountryExists             — nav items reference real countries
 * 0xFC005  NoDuplicateLeaguePerCountry         — no duplicate leagues per country
 * 0xFC006  MatchDateFormatIsValid              — date matches YYYY-MM-DD
 * 0xFC007  MatchTimeFormatIsValid              — time matches HH:MM
 * 0xFC008  MatchFieldsNotEmpty                 — no empty team/city/stadium strings
 * 0xFC009  SeasonFormatIsValid                 — season matches YYYY-YYYY
 * 0xFC010  NavigationNoDuplicates              — no duplicate nav items
 * 0xFC011  CountryNameStartUpperCase           — country name starts uppercase
 * 0xFC012  NavigationMatchesAllCountries       — every country appears in nav
 * 0xFC013  MatchDateWithinSeason               — match date year within season range
 * 0xFC014  StadiumNameMinLength                — stadium name ≥ 3 characters
 * 0xFC015  CountryHasAtLeastOneLeague          — no empty country blocks
 * 0xFC016  LeagueHasAtLeastOneMatch            — no empty league blocks
 * 0xFC017  UniqueMatchPerLeague                — no duplicate (date,time,home,away) tuples
 * 0xFC018  NavigationNotEmpty                  — navigation must list ≥ 1 country
 * 0xFC019  ScoreNonNegative                    — scores ≥ 0
 * 0xFC020  ScoreReasonableUpperBound           — scores ≤ 99
 * 0xFC021  CountryNameLengthLimit              — country name ≤ 40 chars
 * 0xFC022  LeagueNameLengthLimit               — league name ≤ 40 chars
 * 0xFC023  CityNameNotBlank                    — city strings not whitespace-only
 * 0xFC024  SeasonYearsConsecutive              — season end = start + 1
 * 0xFC025  MatchTimeGranularity                — minutes must be 00/15/30/45
 * 0xFC026  LeagueSeasonConsistentWithinCountry — all leagues in a country use same season
 * 0xFC027  MaxMatchesPerLeague                 — ≤ 380 matches per league
 * </pre>
 */
public class FootballSiteCoCos {

    /**
     * Create a fully configured CoCo checker with all 27 context conditions.
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

        // ── Phase 6 — extended CoCos (0xFC013–0xFC027) ───────────────
        checker.addCoCo(new MatchDateWithinSeason());        // 0xFC013
        checker.addCoCo(new StadiumNameMinLength());         // 0xFC014
        checker.addCoCo(new CountryHasAtLeastOneLeague());   // 0xFC015
        checker.addCoCo(new LeagueHasAtLeastOneMatch());     // 0xFC016
        checker.addCoCo(new UniqueMatchPerLeague());         // 0xFC017
        checker.addCoCo(new NavigationNotEmpty());           // 0xFC018
        checker.addCoCo(new ScoreNonNegative());             // 0xFC019
        checker.addCoCo(new ScoreReasonableUpperBound());    // 0xFC020
        checker.addCoCo(new CountryNameLengthLimit());       // 0xFC021
        checker.addCoCo(new LeagueNameLengthLimit());        // 0xFC022
        checker.addCoCo(new CityNameNotBlank());             // 0xFC023
        checker.addCoCo(new SeasonYearsConsecutive());       // 0xFC024
        checker.addCoCo(new MatchTimeGranularity());         // 0xFC025
        checker.addCoCo(new LeagueSeasonConsistentWithinCountry()); // 0xFC026
        checker.addCoCo(new MaxMatchesPerLeague());          // 0xFC027

        return checker;
    }
}
