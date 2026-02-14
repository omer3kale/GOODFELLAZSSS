package football.cocos;

import football.footballsite._ast.ASTNavigation;
import football.footballsite._cocos.FootballSiteASTNavigationCoCo;
import de.se_rwth.commons.logging.Log;

/**
 * CoCo 0xFC018: Navigation block must contain at least one country item.
 * An empty navigation produces a site with no navigable links.
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
