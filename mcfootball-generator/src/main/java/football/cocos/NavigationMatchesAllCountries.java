package football.cocos;

import football.footballsite._ast.ASTCountry;
import football.footballsite._ast.ASTFootballSite;
import football.footballsite._ast.ASTNavigationItem;
import football.footballsite._cocos.FootballSiteASTFootballSiteCoCo;
import de.se_rwth.commons.logging.Log;

import java.util.HashSet;
import java.util.Set;

/**
 * CoCo: Every declared country must appear as a navigation item.
 * <p>
 * This is the inverse of {@link NavigationCountryExists} (0xFC004).
 * Together they ensure a 1-to-1 correspondence between navigation
 * items and country blocks.
 */
public class NavigationMatchesAllCountries implements FootballSiteASTFootballSiteCoCo {

    @Override
    public void check(ASTFootballSite node) {
        if (!node.isPresentNavigation()) {
            return;
        }

        // Collect navigation item names
        Set<String> navNames = new HashSet<>();
        for (ASTNavigationItem item : node.getNavigation().getNavigationItemList()) {
            navNames.add(item.getName());
        }

        // Every country must appear in navigation
        for (ASTCountry country : node.getCountryList()) {
            String name = country.getName();
            if (!navNames.contains(name)) {
                Log.error("0xFC012 Country '" + name
                        + "' at " + country.get_SourcePositionStart()
                        + " is not listed in the navigation block.");
            }
        }
    }
}
