package football.cocos;

import football.footballsite._ast.ASTNavigation;
import football.footballsite._ast.ASTNavigationItem;
import football.footballsite._cocos.FootballSiteASTNavigationCoCo;
import de.se_rwth.commons.logging.Log;

import java.util.HashSet;
import java.util.Set;

/**
 * CoCo: No two navigation items may have the same name.
 * Duplicate links in the nav bar are pointless and likely a copy-paste error.
 */
public class NavigationNoDuplicates implements FootballSiteASTNavigationCoCo {

    @Override
    public void check(ASTNavigation node) {
        Set<String> seen = new HashSet<>();
        for (ASTNavigationItem item : node.getNavigationItemList()) {
            String name = item.getName();
            if (!seen.add(name)) {
                Log.error("0xFC010 Duplicate navigation item '" + name
                        + "' at " + item.get_SourcePositionStart()
                        + ". Each country should appear at most once in navigation.");
            }
        }
    }
}
