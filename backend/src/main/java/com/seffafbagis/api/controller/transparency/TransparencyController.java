package com.seffafbagis.api.controller.transparency;

import com.seffafbagis.api.dto.response.transparency.ScoreHistoryListResponse;
import com.seffafbagis.api.dto.response.transparency.TransparencyLeaderboardResponse;
import com.seffafbagis.api.dto.response.transparency.TransparencyScoreResponse;
import com.seffafbagis.api.exception.ResourceNotFoundException;
import com.seffafbagis.api.repository.OrganizationRepository;
import com.seffafbagis.api.service.transparency.TransparencyScoreService;
import com.seffafbagis.api.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Transparency Score", description = "Endpoints for organization transparency scores")
public class TransparencyController {

    private final TransparencyScoreService scoreService;
    private final OrganizationRepository organizationRepository;

    @Operation(summary = "Get organization score")
    @GetMapping("/transparency/organization/{id}")
    public ResponseEntity<TransparencyScoreResponse> getOrganizationScore(@PathVariable UUID id) {
        return ResponseEntity.ok(scoreService.getOrganizationScore(id));
    }

    @Operation(summary = "Get organization score history")
    @GetMapping("/transparency/organization/{id}/history")
    public ResponseEntity<ScoreHistoryListResponse> getScoreHistory(
            @PathVariable UUID id,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(scoreService.getScoreHistory(id, pageable));
    }

    @Operation(summary = "Get transparency leaderboard")
    @GetMapping("/transparency/leaderboard")
    public ResponseEntity<List<TransparencyLeaderboardResponse>> getLeaderboard(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(scoreService.getLeaderboard(pageable));
    }

    @Operation(summary = "Get my organization score")
    @GetMapping("/transparency/my")
    @PreAuthorize("hasRole('FOUNDATION')")
    public ResponseEntity<TransparencyScoreResponse> getMyScore() {
        UUID organizationId = getCurrentUserOrganizationId();
        return ResponseEntity.ok(scoreService.getOrganizationScore(organizationId));
    }

    @Operation(summary = "Get my organization score history")
    @GetMapping("/transparency/my/history")
    @PreAuthorize("hasRole('FOUNDATION')")
    public ResponseEntity<ScoreHistoryListResponse> getMyScoreHistory(
            @PageableDefault(size = 20) Pageable pageable) {
        UUID organizationId = getCurrentUserOrganizationId();
        return ResponseEntity.ok(scoreService.getScoreHistory(organizationId, pageable));
    }

    @Operation(summary = "Check if can create campaign")
    @GetMapping("/transparency/my/can-create-campaign")
    @PreAuthorize("hasRole('FOUNDATION')")
    public ResponseEntity<Boolean> canCreateCampaign() {
        UUID organizationId = getCurrentUserOrganizationId();
        return ResponseEntity.ok(scoreService.canCreateCampaign(organizationId));
    }

    // ============ Admin Endpoints ============

    @Operation(summary = "Get organizations with low transparency score")
    @GetMapping("/admin/transparency/low-score")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<TransparencyScoreResponse>> getLowScoreOrganizations(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(scoreService.getLowScoreOrganizations(pageable));
    }

    @Operation(summary = "Force recalculate organization score")
    @PostMapping("/admin/transparency/{orgId}/recalculate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TransparencyScoreResponse> recalculateScore(@PathVariable UUID orgId) {
        scoreService.recalculateScore(orgId);
        return ResponseEntity.ok(scoreService.getOrganizationScore(orgId));
    }

    private UUID getCurrentUserOrganizationId() {
        UUID userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new ResourceNotFoundException("User not authenticated"));

        return organizationRepository.findByUserId(userId)
                .map(org -> org.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found for current user"));
    }
}
