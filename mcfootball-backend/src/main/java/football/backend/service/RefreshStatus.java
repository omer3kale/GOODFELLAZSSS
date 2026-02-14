package football.backend.service;

/**
 * Outcome status for a full refresh cycle.
 */
public enum RefreshStatus {

    /** All countries fetched, CoCos passed, model written. */
    SUCCESS,

    /** Too many leagues failed (below minSuccessRate threshold). */
    FETCH_BELOW_THRESHOLD,

    /** CoCo validation of the generated model failed. */
    COCO_VALIDATION_FAILED,

    /** File I/O error when writing the .fb model. */
    WRITE_ERROR
}
