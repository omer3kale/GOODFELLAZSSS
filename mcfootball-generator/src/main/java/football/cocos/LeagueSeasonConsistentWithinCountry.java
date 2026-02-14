package football.cocos;

import football.footballsite._ast.ASTCountry;
import football.footballsite._ast.ASTLeague;
import football.footballsite._cocos.FootballSiteASTCountryCoCo;
import de.se_rwth.commons.logging.Log;

import java.util.List;

/**
 * CoCo 0xFC026: All leagues within a country must use the same season string.
 * Mixed seasons (e.g., "2025-2026" and "2024-2025") in one country
 * indicate an inconsistency in the data model.
 */
public class LeagueSeasonConsistentWithinCountry implements FootballSiteASTCountryCoCo {

    @Override
    public void check(ASTCountry node) {
        List<ASTLeague> leagues = node.getLeagueList();
        if (leagues.size() <= 1) {
            return; // Nothing to compare
        }
        String firstSeason = leagues.get(0).getSeason();
        for (int i = 1; i < leagues.size(); i++) {
            ASTLeague league = leagues.get(i);
            if (!firstSeason.equals(league.getSeason())) {
                Log.error("0xFC026 League '" + league.getName()
                        + "' at " + league.get_SourcePositionStart()
                        + " has season '" + league.getSeason()
                        + "' which differs from '" + firstSeason
                        + "' used by other leagues in country '"
                        + node.getName() + "'.");
            }
        }
    }
}
