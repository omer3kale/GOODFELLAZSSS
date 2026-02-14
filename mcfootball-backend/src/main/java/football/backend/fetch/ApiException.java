package football.backend.fetch;

/**
 * Exception thrown when an external football API call fails.
 * Covers HTTP errors, rate limits, timeouts, and unparseable responses.
 */
public class ApiException extends RuntimeException {

    public ApiException(String message) {
        super(message);
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
