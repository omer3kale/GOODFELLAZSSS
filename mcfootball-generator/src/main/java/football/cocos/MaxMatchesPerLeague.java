package football.cocos;

import football.footballsite._ast.ASTLeague;
import football.footballsite._cocos.FootballSiteASTLeagueCoCo;
import de.se_rwth.commons.logging.Log;

/**
 * CoCo 0xFC027: A league must not exceed 380 matches.
 * 380 is a reasonable upper bound (20-team league, 38 rounds × 10 games).
 * Exceeding this suggests a data error or runaway generation.
 */
public class MaxMatchesPerLeague implements FootballSiteASTLeagueCoCo {

    private static final int MAX_MATCHES = 380;

    @Override
    public void check(ASTLeague node) {
        int count = node.getMatchList().size();
        if (count > MAX_MATCHES) {
            Log.error("0xFC027 League '" + node.getName()
                    + "' at " + node.get_SourcePositionStart()
                    + " has " + count + " matches (maximum " + MAX_MATCHES + ").");
        }
    }
}
