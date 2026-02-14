package football.cocos;

import football.footballsite._ast.ASTMatch;
import football.footballsite._cocos.FootballSiteASTMatchCoCo;
import de.se_rwth.commons.logging.Log;

/**
 * CoCo: The homeTeam and awayTeam of a match must differ (case-insensitive).
 */
public class MatchHasTwoDifferentTeams implements FootballSiteASTMatchCoCo {

    @Override
    public void check(ASTMatch node) {
        String home = node.getHomeTeam();
        String away = node.getAwayTeam();
        if (home.equalsIgnoreCase(away)) {
            Log.error("0xFC003 Match at " + node.get_SourcePositionStart()
                    + " has identical home and away team '" + home + "'.");
        }
    }
}
