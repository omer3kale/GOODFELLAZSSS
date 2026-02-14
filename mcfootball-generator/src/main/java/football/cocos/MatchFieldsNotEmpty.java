package football.cocos;

import football.footballsite._ast.ASTMatch;
import football.footballsite._cocos.FootballSiteASTMatchCoCo;
import de.se_rwth.commons.logging.Log;

/**
 * CoCo: Match string fields (homeTeam, awayTeam, homeCity, awayCity, stadium)
 * must not be empty.
 */
public class MatchFieldsNotEmpty implements FootballSiteASTMatchCoCo {

    @Override
    public void check(ASTMatch node) {
        checkField(node, node.getHomeTeam(), "homeTeam");
        checkField(node, node.getAwayTeam(), "awayTeam");
        checkField(node, node.getHomeCity(), "homeCity");
        checkField(node, node.getAwayCity(), "awayCity");
        checkField(node, node.getStadium(), "stadium");
    }

    private void checkField(ASTMatch node, String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            Log.error("0xFC008 Empty '" + fieldName
                    + "' in match at " + node.get_SourcePositionStart()
                    + ". All match fields must be non-empty.");
        }
    }
}
