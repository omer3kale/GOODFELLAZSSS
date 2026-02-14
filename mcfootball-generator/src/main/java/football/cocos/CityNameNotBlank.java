package football.cocos;

import football.footballsite._ast.ASTMatch;
import football.footballsite._cocos.FootballSiteASTMatchCoCo;
import de.se_rwth.commons.logging.Log;

/**
 * CoCo 0xFC023: City name strings must not be blank (all whitespace).
 * Distinct from 0xFC008 (MatchFieldsNotEmpty) which checks for truly empty
 * strings; this catches strings that are only whitespace, e.g., "   ".
 */
public class CityNameNotBlank implements FootballSiteASTMatchCoCo {

    @Override
    public void check(ASTMatch node) {
        checkCity(node, node.getHomeCity(), "homeCity");
        checkCity(node, node.getAwayCity(), "awayCity");
    }

    private void checkCity(ASTMatch node, String city, String fieldName) {
        if (city != null && !city.isEmpty() && city.trim().isEmpty()) {
            Log.error("0xFC023 Field '" + fieldName
                    + "' in match at " + node.get_SourcePositionStart()
                    + " contains only whitespace. City names must not be blank.");
        }
    }
}
