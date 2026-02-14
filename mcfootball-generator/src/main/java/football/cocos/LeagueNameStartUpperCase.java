package football.cocos;

import football.footballsite._ast.ASTLeague;
import football.footballsite._cocos.FootballSiteASTLeagueCoCo;
import de.se_rwth.commons.logging.Log;

/**
 * CoCo: Every league Name must start with an uppercase letter.
 */
public class LeagueNameStartUpperCase implements FootballSiteASTLeagueCoCo {

    @Override
    public void check(ASTLeague node) {
        String name = node.getName();
        if (!name.isEmpty() && !Character.isUpperCase(name.charAt(0))) {
            Log.error("0xFC002 League name '" + name
                    + "' at " + node.get_SourcePositionStart()
                    + " does not start with an uppercase letter.");
        }
    }
}
