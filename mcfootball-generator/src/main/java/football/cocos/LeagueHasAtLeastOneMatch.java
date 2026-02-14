package football.cocos;

import football.footballsite._ast.ASTLeague;
import football.footballsite._cocos.FootballSiteASTLeagueCoCo;
import de.se_rwth.commons.logging.Log;

/**
 * CoCo 0xFC016: A league must contain at least one match.
 * Empty leagues produce blank HTML pages and are likely incomplete data.
 */
public class LeagueHasAtLeastOneMatch implements FootballSiteASTLeagueCoCo {

    @Override
    public void check(ASTLeague node) {
        if (node.isEmptyMatchs()) {
            Log.error("0xFC016 League '" + node.getName()
                    + "' at " + node.get_SourcePositionStart()
                    + " has no matches. Each league must contain at least one match.");
        }
    }
}
