package football.backend.validation;

import java.util.List;

/**
 * Result of validating a generated .fb model string against the
 * FootballSite.mc4 grammar and CoCo rules.
 */
public class ValidationResult {

    private final boolean valid;
    private final int errorCount;
    private final List<String> errors;

    public ValidationResult(boolean valid, int errorCount, List<String> errors) {
        this.valid = valid;
        this.errorCount = errorCount;
        this.errors = errors;
    }

    public boolean isValid()       { return valid; }
    public int getErrorCount()     { return errorCount; }
    public List<String> getErrors() { return errors; }

    /** Factory for a passing result. */
    public static ValidationResult ok() {
        return new ValidationResult(true, 0, List.of());
    }

    /** Factory for a failing result. */
    public static ValidationResult fail(int errorCount, List<String> errors) {
        return new ValidationResult(false, errorCount, errors);
    }
}
