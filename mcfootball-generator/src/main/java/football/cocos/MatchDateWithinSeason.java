package football.cocos;

import football.footballsite._ast.ASTLeague;
import football.footballsite._ast.ASTMatch;
import football.footballsite._cocos.FootballSiteASTLeagueCoCo;
import de.se_rwth.commons.logging.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CoCo 0xFC013: Match date year must fall within the season's start and end years.
 * E.g., season "2025-2026" allows match dates in 2025 and 2026 only.
 */
public class MatchDateWithinSeason implements FootballSiteASTLeagueCoCo {

    private static final Pattern SEASON_PATTERN = Pattern.compile("(\\d{4})-(\\d{4})");
    private static final Pattern DATE_PATTERN = Pattern.compile("(\\d{4})-\\d{2}-\\d{2}");

    @Override
    public void check(ASTLeague node) {
        Matcher seasonMatcher = SEASON_PATTERN.matcher(node.getSeason());
        if (!seasonMatcher.matches()) {
            return; // Season format invalid — handled by 0xFC009
        }
        int startYear = Integer.parseInt(seasonMatcher.group(1));
        int endYear = Integer.parseInt(seasonMatcher.group(2));

        for (ASTMatch match : node.getMatchList()) {
            Matcher dateMatcher = DATE_PATTERN.matcher(match.getMatchDate());
            if (!dateMatcher.matches()) {
                continue; // Date format invalid — handled by 0xFC006
            }
            int matchYear = Integer.parseInt(dateMatcher.group(1));
            if (matchYear < startYear || matchYear > endYear) {
                Log.error("0xFC013 Match date '" + match.getMatchDate()
                        + "' at " + match.get_SourcePositionStart()
                        + " is outside season '" + node.getSeason()
                        + "' in league '" + node.getName() + "'.");
            }
        }
    }
}
