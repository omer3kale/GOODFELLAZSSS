package football.cocos;

import football.footballsite._ast.ASTCountry;
import football.footballsite._ast.ASTFootballSite;
import football.footballsite._cocos.FootballSiteASTFootballSiteCoCo;
import de.se_rwth.commons.logging.Log;

import java.util.HashSet;
import java.util.Set;

/**
 * CoCo: No two country blocks may share the same Name within a footballsite.
 */
public class CountryNameIsUnique implements FootballSiteASTFootballSiteCoCo {

    @Override
    public void check(ASTFootballSite node) {
        Set<String> seen = new HashSet<>();
        for (ASTCountry country : node.getCountryList()) {
            String name = country.getName();
            if (!seen.add(name)) {
                Log.error("0xFC001 Duplicate country name '" + name
                        + "' at " + country.get_SourcePositionStart()
                        + ". Country names must be unique.");
            }
        }
    }
}
