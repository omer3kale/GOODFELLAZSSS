package football.cocos;

import football.footballsite._ast.ASTLeague;
import football.footballsite._cocos.FootballSiteASTLeagueCoCo;
import de.se_rwth.commons.logging.Log;

import java.util.regex.Pattern;

/**
 * CoCo: The league season string must match the pattern YYYY-YYYY
 * (e.g., "2025-2026").
 */
public class SeasonFormatIsValid implements FootballSiteASTLeagueCoCo {

    private static final Pattern SEASON_PATTERN =
            Pattern.compile("\\d{4}-\\d{4}");

    @Override
    public void check(ASTLeague node) {
        String season = node.getSeason();
        if (!SEASON_PATTERN.matcher(season).matches()) {
            Log.error("0xFC009 Invalid season format '" + season
                    + "' in league '" + node.getName()
                    + "' at " + node.get_SourcePositionStart()
                    + ". Expected YYYY-YYYY.");
        }
    }
}
