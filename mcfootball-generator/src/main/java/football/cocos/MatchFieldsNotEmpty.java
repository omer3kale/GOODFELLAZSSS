package football.cocos;

import football.footballsite._ast.ASTMatch;
import football.footballsite._cocos.FootballSiteASTMatchCoCo;
import de.se_rwth.commons.logging.Log;

/**
 * CoCo 0xFC008: Match string fields (homeTeam, awayTeam, homeCity, awayCity, stadium)
 * must not be empty (zero-length).
 *
 * <p>This checks for truly empty strings ({@code ""}). Whitespace-only strings
 * (e.g. {@code " "}) are handled separately by
 * {@link CityNameNotBlank} (0xFC023) for cities
 * and {@link StadiumNameMinLength} (0xFC014) for stadiums.
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
        if (value == null || value.isEmpty()) {
            Log.error("0xFC008 Empty '" + fieldName
                    + "' in match at " + node.get_SourcePositionStart()
                    + ". All match fields must be non-empty.");
        }
    }
}
