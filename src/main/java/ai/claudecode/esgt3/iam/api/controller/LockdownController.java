package ai.claudecode.esgt3.iam.api.controller;

import ai.claudecode.esgt3.iam.api.LockdownRequest;
import ai.claudecode.esgt3.iam.infra.LockdownState;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/lockdown")
@RequiredArgsConstructor
public class LockdownController {

    private final LockdownState state;

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/{tenantId}")
    public ResponseEntity<Map<String, Object>> activate(@PathVariable UUID tenantId,
                                                        @Valid @RequestBody LockdownRequest req) {
        state.activate(tenantId, req.reason());
        return ResponseEntity.ok(Map.of("tenantId", tenantId, "locked", true, "reason", req.reason()));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("/{tenantId}")
    public ResponseEntity<Map<String, Object>> deactivate(@PathVariable UUID tenantId,
                                                          @Valid @RequestBody LockdownRequest req) {
        state.deactivate(tenantId, req.reason());
        return ResponseEntity.ok(Map.of("tenantId", tenantId, "locked", false, "reason", req.reason()));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping
    public Map<String, Object> list() {
        return Map.of("lockedTenants", state.snapshot());
    }
}
