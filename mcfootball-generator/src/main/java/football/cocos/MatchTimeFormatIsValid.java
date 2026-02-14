package football.cocos;

import football.footballsite._ast.ASTMatch;
import football.footballsite._cocos.FootballSiteASTMatchCoCo;
import de.se_rwth.commons.logging.Log;

import java.util.regex.Pattern;

/**
 * CoCo: The matchTime string must be a valid HH:MM (24-hour clock).
 * Hours 00-23, minutes 00-59.
 */
public class MatchTimeFormatIsValid implements FootballSiteASTMatchCoCo {

    // Matches 00:00 – 23:59
    private static final Pattern TIME_PATTERN =
            Pattern.compile("([01]\\d|2[0-3]):[0-5]\\d");

    @Override
    public void check(ASTMatch node) {
        String time = node.getMatchTime();
        if (!TIME_PATTERN.matcher(time).matches()) {
            Log.error("0xFC007 Invalid time format '" + time
                    + "' in match at " + node.get_SourcePositionStart()
                    + ". Expected HH:MM (00:00 – 23:59).");
        }
    }
}
