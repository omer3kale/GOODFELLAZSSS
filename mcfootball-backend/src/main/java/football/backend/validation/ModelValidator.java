package football.backend.validation;

import de.monticore.ast.ASTNode;
import de.se_rwth.commons.logging.Log;
import de.se_rwth.commons.logging.Finding;
import football.cocos.FootballSiteCoCos;
import football.footballsite.FootballSiteMill;
import football.footballsite._cocos.FootballSiteCoCoChecker;
import football.footballsite._parser.FootballSiteParser;
import football.footballsite._ast.ASTFootballSite;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Validates a .fb model string by parsing it with the generated
 * MontiCore parser and running the full CoCo checker.
 * <p>
 * Uses in-memory parsing ({@code parse_StringFootballSite}) so no
 * temporary file I/O is needed.
 */
@Component
public class ModelValidator {

    /**
     * Parse and CoCo-check the given .fb model string.
     *
     * @param modelContent the full .fb file content
     * @return validation result with any parser or CoCo errors
     */
    public ValidationResult validate(String modelContent) {
        // Ensure MontiCore doesn't kill the JVM on first error
        Log.enableFailQuick(false);

        // Snapshot current error count so we detect only new errors
        long errorsBefore = Log.getErrorCount();
        List<Finding> findingsBefore = List.copyOf(Log.getFindings());

        try {
            FootballSiteParser parser = new FootballSiteParser();
            Optional<ASTFootballSite> optAst = parser.parse_StringFootballSite(modelContent);

            if (optAst.isEmpty()) {
                // Parser failed — collect new findings
                List<String> newErrors = collectNewErrors(findingsBefore);
                if (newErrors.isEmpty()) {
                    newErrors = List.of("Failed to parse model (unknown parser error)");
                }
                return ValidationResult.fail(newErrors.size(), newErrors);
            }

            // Run CoCo checks
            FootballSiteCoCoChecker checker = FootballSiteCoCos.createChecker();
            checker.checkAll(optAst.get());

            long errorsAfter = Log.getErrorCount();
            int newErrorCount = (int) (errorsAfter - errorsBefore);

            if (newErrorCount > 0) {
                List<String> newErrors = collectNewErrors(findingsBefore);
                return ValidationResult.fail(newErrorCount, newErrors);
            }

            return ValidationResult.ok();

        } catch (IOException e) {
            return ValidationResult.fail(1,
                    List.of("Parser I/O error: " + e.getMessage()));
        }
    }

    /**
     * Collect error messages that appeared after the baseline snapshot.
     */
    private List<String> collectNewErrors(List<Finding> baseline) {
        List<Finding> current = Log.getFindings();
        return current.stream()
                .filter(f -> !baseline.contains(f))
                .filter(Finding::isError)
                .map(Finding::getMsg)
                .collect(Collectors.toList());
    }
}
