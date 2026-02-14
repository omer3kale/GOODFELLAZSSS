package football.cocos;

import football.footballsite._ast.ASTCountry;
import football.footballsite._ast.ASTLeague;
import football.footballsite._cocos.FootballSiteASTCountryCoCo;
import de.se_rwth.commons.logging.Log;

import java.util.HashSet;
import java.util.Set;

/**
 * CoCo: Within a single country, no two league blocks may share the same Name.
 */
public class NoDuplicateLeaguePerCountry implements FootballSiteASTCountryCoCo {

    @Override
    public void check(ASTCountry node) {
        Set<String> seen = new HashSet<>();
        for (ASTLeague league : node.getLeagueList()) {
            String name = league.getName();
            if (!seen.add(name)) {
                Log.error("0xFC005 Duplicate league name '" + name
                        + "' in country '" + node.getName()
                        + "' at " + league.get_SourcePositionStart() + ".");
            }
        }
    }
}
