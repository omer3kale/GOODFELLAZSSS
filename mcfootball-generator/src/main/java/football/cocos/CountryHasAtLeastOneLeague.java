package football.cocos;

import football.footballsite._ast.ASTCountry;
import football.footballsite._cocos.FootballSiteASTCountryCoCo;
import de.se_rwth.commons.logging.Log;

/**
 * CoCo 0xFC015: A country block must contain at least one league.
 * Empty country blocks serve no purpose and are likely accidental.
 *
 * <p><strong>Grammar-unreachable:</strong> The grammar rule
 * {@code Country = "country" Name "{" League+ "}";} enforces at least one
 * league at parse time. This CoCo exists as a defensive guard for
 * programmatic AST construction and as a teaching example of a "spec
 * contract" that mirrors a grammar constraint.
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
