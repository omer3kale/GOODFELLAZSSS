package football.cocos;

import football.footballsite._ast.ASTLeague;
import football.footballsite._ast.ASTMatch;
import football.footballsite._cocos.FootballSiteASTLeagueCoCo;
import de.se_rwth.commons.logging.Log;

import java.util.HashSet;
import java.util.Set;

/**
 * CoCo 0xFC017: No duplicate matches within a league.
 * A match is uniquely identified by the tuple (date, time, homeTeam, awayTeam).
 */
public class UniqueMatchPerLeague implements FootballSiteASTLeagueCoCo {

    @Override
    public void check(ASTLeague node) {
        Set<String> seen = new HashSet<>();
        for (ASTMatch match : node.getMatchList()) {
            String key = match.getMatchDate() + "|" + match.getMatchTime()
                    + "|" + match.getHomeTeam() + "|" + match.getAwayTeam();
            if (!seen.add(key)) {
                Log.error("0xFC017 Duplicate match (date='" + match.getMatchDate()
                        + "', time='" + match.getMatchTime()
                        + "', home='" + match.getHomeTeam()
                        + "', away='" + match.getAwayTeam()
                        + "') in league '" + node.getName()
                        + "' at " + match.get_SourcePositionStart() + ".");
            }
        }
    }
}
