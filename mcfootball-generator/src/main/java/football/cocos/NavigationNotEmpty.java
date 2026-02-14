package football.cocos;

import football.footballsite._ast.ASTNavigation;
import football.footballsite._cocos.FootballSiteASTNavigationCoCo;
import de.se_rwth.commons.logging.Log;

/**
 * CoCo 0xFC018: Navigation block must contain at least one country item.
 * An empty navigation produces a site with no navigable links.
 *
 * <p><strong>Grammar-unreachable:</strong> The grammar rule
 * {@code Navigation = "navigation" "{" NavigationItem (";" NavigationItem)* ";"? "}";}
 * requires at least one {@code NavigationItem} at parse time.
 * This CoCo exists as a defensive guard for programmatic AST construction.
 */
public class NavigationNotEmpty implements FootballSiteASTNavigationCoCo {

    @Override
    public void check(ASTNavigation node) {
        if (node.isEmptyNavigationItems()) {
            Log.error("0xFC018 Navigation block at "
                    + node.get_SourcePositionStart()
                    + " is empty. At least one country must be listed.");
        }
    }
}
