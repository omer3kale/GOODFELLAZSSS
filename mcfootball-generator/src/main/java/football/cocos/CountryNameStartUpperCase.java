package football.cocos;

import football.footballsite._ast.ASTCountry;
import football.footballsite._cocos.FootballSiteASTCountryCoCo;
import de.se_rwth.commons.logging.Log;

/**
 * CoCo: Every country Name must start with an uppercase letter.
 * Parallel to LeagueNameStartUpperCase (0xFC002) but for countries.
 */
public class CountryNameStartUpperCase implements FootballSiteASTCountryCoCo {

    @Override
    public void check(ASTCountry node) {
        String name = node.getName();
        if (!name.isEmpty() && !Character.isUpperCase(name.charAt(0))) {
            Log.error("0xFC011 Country name '" + name
                    + "' at " + node.get_SourcePositionStart()
                    + " does not start with an uppercase letter.");
        }
    }
}
