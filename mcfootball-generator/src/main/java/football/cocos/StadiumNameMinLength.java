package football.cocos;

import football.footballsite._ast.ASTMatch;
import football.footballsite._cocos.FootballSiteASTMatchCoCo;
import de.se_rwth.commons.logging.Log;

/**
 * CoCo 0xFC014: Stadium name must be at least 3 characters long.
 * Single-char or two-char stadium names are likely typos.
 */
public class StadiumNameMinLength implements FootballSiteASTMatchCoCo {

    private static final int MIN_LENGTH = 3;

    @Override
    public void check(ASTMatch node) {
        String stadium = node.getStadium();
        if (stadium != null && stadium.trim().length() < MIN_LENGTH) {
            Log.error("0xFC014 Stadium name '" + stadium
                    + "' in match at " + node.get_SourcePositionStart()
                    + " is too short (minimum " + MIN_LENGTH + " characters).");
        }
    }
}
