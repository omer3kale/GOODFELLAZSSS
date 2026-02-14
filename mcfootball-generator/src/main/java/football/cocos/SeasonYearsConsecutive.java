package football.cocos;

import football.footballsite._ast.ASTLeague;
import football.footballsite._cocos.FootballSiteASTLeagueCoCo;
import de.se_rwth.commons.logging.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CoCo 0xFC024: Season end year must be exactly start year + 1.
 * E.g., "2025-2026" is valid; "2025-2028" or "2026-2025" is not.
 */
public class SeasonYearsConsecutive implements FootballSiteASTLeagueCoCo {

    private static final Pattern SEASON_PATTERN = Pattern.compile("(\\d{4})-(\\d{4})");

    @Override
    public void check(ASTLeague node) {
        Matcher m = SEASON_PATTERN.matcher(node.getSeason());
        if (!m.matches()) {
            return; // Format issue handled by 0xFC009
        }
        int start = Integer.parseInt(m.group(1));
        int end = Integer.parseInt(m.group(2));
        if (end != start + 1) {
            Log.error("0xFC024 Season '" + node.getSeason()
                    + "' in league '" + node.getName()
                    + "' at " + node.get_SourcePositionStart()
                    + " does not have consecutive years (expected "
                    + start + "-" + (start + 1) + ").");
        }
    }
}
