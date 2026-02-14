package football.cocos;

import football.footballsite._ast.ASTCountry;
import football.footballsite._cocos.FootballSiteASTCountryCoCo;
import de.se_rwth.commons.logging.Log;

/**
 * CoCo 0xFC015: A country block must contain at least one league.
 * Empty country blocks serve no purpose and are likely accidental.
 */
public class CountryHasAtLeastOneLeague implements FootballSiteASTCountryCoCo {

    @Override
    public void check(ASTCountry node) {
        if (node.isEmptyLeagues()) {
            Log.error("0xFC015 Country '" + node.getName()
                    + "' at " + node.get_SourcePositionStart()
                    + " has no leagues. Each country must contain at least one league.");
        }
    }
}
