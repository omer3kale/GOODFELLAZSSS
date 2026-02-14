package football.cocos;

import football.footballsite._ast.ASTMatch;
import football.footballsite._cocos.FootballSiteASTMatchCoCo;
import de.se_rwth.commons.logging.Log;

import java.util.regex.Pattern;

/**
 * CoCo: The matchDate string must match the pattern YYYY-MM-DD (ISO 8601 date).
 */
public class MatchDateFormatIsValid implements FootballSiteASTMatchCoCo {

    private static final Pattern DATE_PATTERN =
            Pattern.compile("\\d{4}-\\d{2}-\\d{2}");

    @Override
    public void check(ASTMatch node) {
        String date = node.getMatchDate();
        if (!DATE_PATTERN.matcher(date).matches()) {
            Log.error("0xFC006 Invalid date format '" + date
                    + "' in match at " + node.get_SourcePositionStart()
                    + ". Expected YYYY-MM-DD.");
        }
    }
}
