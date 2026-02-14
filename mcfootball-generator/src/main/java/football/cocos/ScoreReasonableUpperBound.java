package football.cocos;

import football.footballsite._ast.ASTMatch;
import football.footballsite._cocos.FootballSiteASTMatchCoCo;
import de.se_rwth.commons.logging.Log;

/**
 * CoCo 0xFC020: Match scores must not exceed 99.
 * Scores above this are unrealistic for football and likely data errors.
 */
public class ScoreReasonableUpperBound implements FootballSiteASTMatchCoCo {

    private static final int MAX_SCORE = 99;

    @Override
    public void check(ASTMatch node) {
        int homeScore = node.getHomeScore().getValue();
        int awayScore = node.getAwayScore().getValue();

        if (homeScore > MAX_SCORE) {
            Log.error("0xFC020 Home score (" + homeScore
                    + ") exceeds maximum " + MAX_SCORE
                    + " in match at " + node.get_SourcePositionStart() + ".");
        }
        if (awayScore > MAX_SCORE) {
            Log.error("0xFC020 Away score (" + awayScore
                    + ") exceeds maximum " + MAX_SCORE
                    + " in match at " + node.get_SourcePositionStart() + ".");
        }
    }
}
