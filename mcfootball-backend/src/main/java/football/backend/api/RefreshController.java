package football.backend.api;

import football.backend.service.RefreshResult;
import football.backend.service.RefreshService;
import football.backend.service.RefreshStatus;
import football.backend.service.RefreshSummary;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * REST controller for triggering .fb model file generation.
 * <p>
 * POST /refresh/all        → fetch → validate → write AllEurope.fb (production)
 * POST /refresh/all-debug  → hard-coded data → per-country .fb + AllEurope.fb
 * POST /refresh/dummy      → Germany.fb only (legacy)
 */
@RestController
@RequestMapping("/refresh")
public class RefreshController {

    private final RefreshService refreshService;

    public RefreshController(RefreshService refreshService) {
        this.refreshService = refreshService;
    }

    /**
     * Production: fetch → validate → write AllEurope.fb.
     * Returns a rich {@link RefreshResult} with outcome, errors, and counts.
     */
    @PostMapping("/all")
    public ResponseEntity<RefreshResult> refreshAll() {
        RefreshResult result = refreshService.refreshAllEurope();
        if (result.getOutcome() == RefreshStatus.SUCCESS) {
            return ResponseEntity.ok(result);
        }
        // Non-success outcomes → 422 Unprocessable Entity
        return ResponseEntity.unprocessableEntity().body(result);
    }

    /**
     * Debug: write per-country .fb files plus AllEurope.fb (hard-coded data).
     */
    @PostMapping("/all-debug")
    public ResponseEntity<RefreshSummary> refreshAllDebug() {
        try {
            RefreshSummary summary = refreshService.refreshAllEuropeDebug();
            return ResponseEntity.ok(summary);
        } catch (IOException e) {
            System.err.println("ERROR writing debug models: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Legacy: write Germany-only .fb (backward compat).
     */
    @PostMapping("/dummy")
    public ResponseEntity<RefreshSummary> refreshDummy() {
        try {
            RefreshSummary summary = refreshService.refreshDummyGermany();
            return ResponseEntity.ok(summary);
        } catch (IOException e) {
            System.err.println("ERROR writing .fb model: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
