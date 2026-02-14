package football.cocos;

import football.footballsite._ast.ASTLeague;
import football.footballsite._cocos.FootballSiteASTLeagueCoCo;
import de.se_rwth.commons.logging.Log;

/**
 * CoCo 0xFC022: League name must not exceed 40 characters.
 * Extremely long identifiers break layout and are likely data errors.
 */
public class LeagueNameLengthLimit implements FootballSiteASTLeagueCoCo {

    private static final int MAX_LENGTH = 40;

    @Override
    public void check(ASTLeague node) {
        String name = node.getName();
        if (name.length() > MAX_LENGTH) {
            Log.error("0xFC022 League name '" + name.substring(0, 20) + "...' at "
                    + node.get_SourcePositionStart()
                    + " is " + name.length() + " characters long (maximum " + MAX_LENGTH + ").");
        }
    }
}
