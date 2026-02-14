package football.cocos;

import football.footballsite._ast.ASTMatch;
import football.footballsite._cocos.FootballSiteASTMatchCoCo;
import de.se_rwth.commons.logging.Log;

/**
 * CoCo 0xFC019: Match scores must be non-negative (≥ 0).
 *
 * <p><strong>Grammar-unreachable:</strong> {@code NatLiteral} in MontiCore is
 * unsigned by definition, so negative values cannot appear in parsed .fb files.
 * This CoCo exists as a defensive guard ("spec contract") for programmatic
 * AST construction where scores might be set via setters.
 */
public class ScoreNonNegative implements FootballSiteASTMatchCoCo {

    @Override
    public void check(ASTMatch node) {
        int homeScore = node.getHomeScore().getValue();
        int awayScore = node.getAwayScore().getValue();

        if (homeScore < 0) {
            Log.error("0xFC019 Negative home score (" + homeScore
                    + ") in match at " + node.get_SourcePositionStart()
                    + ". Scores must be non-negative.");
        }
        if (awayScore < 0) {
            Log.error("0xFC019 Negative away score (" + awayScore
                    + ") in match at " + node.get_SourcePositionStart()
                    + ". Scores must be non-negative.");
        }
    }
}
