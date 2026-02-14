package football.cocos;

import football.footballsite._ast.ASTMatch;
import football.footballsite._cocos.FootballSiteASTMatchCoCo;
import de.se_rwth.commons.logging.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CoCo 0xFC025: Match times should use standard football kick-off granularity.
 * Minutes must be 00, 15, 30, or 45.
 */
public class MatchTimeGranularity implements FootballSiteASTMatchCoCo {

    private static final Pattern TIME_PATTERN = Pattern.compile("\\d{2}:(\\d{2})");

    @Override
    public void check(ASTMatch node) {
        Matcher m = TIME_PATTERN.matcher(node.getMatchTime());
        if (!m.matches()) {
            return; // Format issue handled by 0xFC007
        }
        int minutes = Integer.parseInt(m.group(1));
        if (minutes != 0 && minutes != 15 && minutes != 30 && minutes != 45) {
            Log.error("0xFC025 Match time '" + node.getMatchTime()
                    + "' at " + node.get_SourcePositionStart()
                    + " has non-standard minute granularity."
                    + " Expected minutes to be 00, 15, 30, or 45.");
        }
    }
}
