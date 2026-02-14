package football.cocos;

import football.footballsite._ast.ASTCountry;
import football.footballsite._ast.ASTFootballSite;
import football.footballsite._ast.ASTNavigationItem;
import football.footballsite._cocos.FootballSiteASTFootballSiteCoCo;
import de.se_rwth.commons.logging.Log;

import java.util.HashSet;
import java.util.Set;

/**
 * CoCo: Every NavigationItem name must match exactly one country name
 * declared in the same footballsite.
 */
public class NavigationCountryExists implements FootballSiteASTFootballSiteCoCo {

    @Override
    public void check(ASTFootballSite node) {
        if (!node.isPresentNavigation()) {
            return;
        }

        // Collect declared country names
        Set<String> countryNames = new HashSet<>();
        for (ASTCountry country : node.getCountryList()) {
            countryNames.add(country.getName());
        }

        // Check each navigation item
        for (ASTNavigationItem item : node.getNavigation().getNavigationItemList()) {
            String navName = item.getName();
            if (!countryNames.contains(navName)) {
                Log.error("0xFC004 Navigation item '" + navName
                        + "' at " + item.get_SourcePositionStart()
                        + " does not match any declared country.");
            }
        }
    }
}
