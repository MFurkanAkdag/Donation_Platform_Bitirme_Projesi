package com.seffafbagis.api.service.transparency;

import com.seffafbagis.api.dto.response.transparency.ScoreHistoryResponse;
import com.seffafbagis.api.dto.response.transparency.ScoreHistoryListResponse;
import com.seffafbagis.api.dto.response.transparency.TransparencyLeaderboardResponse;
import com.seffafbagis.api.dto.response.transparency.TransparencyScoreResponse;
import com.seffafbagis.api.entity.campaign.Campaign;
import com.seffafbagis.api.entity.evidence.Evidence;
import com.seffafbagis.api.entity.organization.Organization;
import com.seffafbagis.api.entity.transparency.TransparencyScore;
import com.seffafbagis.api.entity.transparency.TransparencyScoreHistory;
import com.seffafbagis.api.exception.ResourceNotFoundException;
import com.seffafbagis.api.exception.BadRequestException;

import com.seffafbagis.api.repository.CampaignRepository;
import com.seffafbagis.api.repository.EvidenceRepository;
import com.seffafbagis.api.repository.OrganizationRepository;
import com.seffafbagis.api.repository.TransparencyScoreHistoryRepository;
import com.seffafbagis.api.repository.TransparencyScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransparencyScoreService {

    private final TransparencyScoreRepository scoreRepository;
    private final TransparencyScoreHistoryRepository historyRepository;
    private final TransparencyScoreCalculator calculator;
    private final OrganizationRepository organizationRepository;
    private final CampaignRepository campaignRepository;
    private final EvidenceRepository evidenceRepository;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;

    @Transactional
    public void initializeScore(UUID organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        if (scoreRepository.findByOrganizationId(organizationId).isPresent()) {
            return;
        }

        TransparencyScore score = TransparencyScore.builder()
                .organization(organization)
                .currentScore(new BigDecimal("50.00"))
                .build();

        scoreRepository.save(score);

        recordHistory(organization, null, new BigDecimal("50.00"), "INITIAL_SCORE", null, null);
    }

    public TransparencyScoreResponse getOrganizationScore(UUID organizationId) {
        TransparencyScore score = scoreRepository.findByOrganizationId(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Transparency score not found"));

        return mapToResponse(score);
    }

    public ScoreHistoryListResponse getScoreHistory(UUID organizationId, Pageable pageable) {
        Page<TransparencyScoreHistory> historyPage = historyRepository
                .findByOrganizationIdOrderByCreatedAtDesc(organizationId, pageable);

        Page<ScoreHistoryResponse> responsePage = historyPage.map(this::mapToHistoryResponse);

        return ScoreHistoryListResponse.from(responsePage);
    }

    public List<TransparencyLeaderboardResponse> getLeaderboard(Pageable pageable) {
        Page<TransparencyScore> topScores = scoreRepository.findTopByOrderByCurrentScoreDesc(pageable);

        // This ranking logic is simple and depends on the page size/number.
        // For a true global rank, we would need a more complex query or update ranks
        // periodically.
        // For now, valid for the first page.
        int startRank = pageable.getPageNumber() * pageable.getPageSize() + 1;

        return topScores.getContent().stream()
                .map(score -> {
                    int index = topScores.getContent().indexOf(score);
                    return TransparencyLeaderboardResponse.builder()
                            .rank(startRank + index)
                            .organizationId(score.getOrganization().getId())
                            .organizationName(score.getOrganization().getLegalName())
                            .logoUrl(score.getOrganization().getLogoUrl())
                            .currentScore(score.getCurrentScore())
                            .completedCampaigns(score.getCompletedCampaigns())
                            .build();
                })
                .collect(Collectors.toList());
    }

    public boolean canCreateCampaign(UUID organizationId) {
        return scoreRepository.findByOrganizationId(organizationId)
                .map(score -> score.getCurrentScore().compareTo(new BigDecimal("40.00")) >= 0)
                .orElse(false); // No score means not initialized or something wrong, fail safely
    }

    @Transactional
    public void onEvidenceApproved(UUID evidenceId, boolean onTime) {
        Evidence evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Evidence not found"));

        UUID organizationId = evidence.getCampaign().getOrganization().getId();
        TransparencyScore score = getScoreEntity(organizationId);

        String reason = onTime ? "EVIDENCE_APPROVED_ON_TIME" : "EVIDENCE_APPROVED_LATE";
        // If it was *way* after deadline, maybe "EVIDENCE_APPROVED_AFTER_DEADLINE" but
        // let's stick to these for now

        BigDecimal change = calculator.calculateChange(reason);
        BigDecimal newScore = calculator.calculateNewScore(score.getCurrentScore(), change);
        BigDecimal oldScore = score.getCurrentScore();

        score.setCurrentScore(newScore);
        score.setApprovedEvidences(score.getApprovedEvidences() + 1);
        if (onTime) {
            score.setOnTimeReports(score.getOnTimeReports() + 1);
        } else {
            score.setLateReports(score.getLateReports() + 1);
        }
        score.setLastCalculatedAt(LocalDateTime.now());

        scoreRepository.save(score);
        recordHistory(score.getOrganization(), oldScore, newScore, reason, evidence.getCampaign(), evidence);
    }

    @Transactional
    public void onEvidenceRejected(UUID evidenceId) {
        Evidence evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Evidence not found"));

        UUID organizationId = evidence.getCampaign().getOrganization().getId();
        TransparencyScore score = getScoreEntity(organizationId);

        String reason = "EVIDENCE_REJECTED";
        BigDecimal change = calculator.calculateChange(reason);
        BigDecimal newScore = calculator.calculateNewScore(score.getCurrentScore(), change);
        BigDecimal oldScore = score.getCurrentScore();

        score.setCurrentScore(newScore);
        score.setRejectedEvidences(score.getRejectedEvidences() + 1);
        score.setLastCalculatedAt(LocalDateTime.now());

        scoreRepository.save(score);
        recordHistory(score.getOrganization(), oldScore, newScore, reason, evidence.getCampaign(), evidence);
    }

    @Transactional
    public void onCampaignCompleted(UUID campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));

        UUID organizationId = campaign.getOrganization().getId();
        TransparencyScore score = getScoreEntity(organizationId);

        String reason = "CAMPAIGN_COMPLETED";
        BigDecimal change = calculator.calculateChange(reason);
        BigDecimal newScore = calculator.calculateNewScore(score.getCurrentScore(), change);
        BigDecimal oldScore = score.getCurrentScore();

        score.setCurrentScore(newScore);
        score.setCompletedCampaigns(score.getCompletedCampaigns() + 1);
        score.setLastCalculatedAt(LocalDateTime.now());

        scoreRepository.save(score);
        recordHistory(score.getOrganization(), oldScore, newScore, reason, campaign, null);
    }

    @Transactional
    public void onCampaignCancelled(UUID campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));

        UUID organizationId = campaign.getOrganization().getId();
        TransparencyScore score = getScoreEntity(organizationId);

        String reason = "CAMPAIGN_CANCELLED";
        BigDecimal change = calculator.calculateChange(reason);
        BigDecimal newScore = calculator.calculateNewScore(score.getCurrentScore(), change);
        BigDecimal oldScore = score.getCurrentScore();

        score.setCurrentScore(newScore);
        score.setLastCalculatedAt(LocalDateTime.now());

        scoreRepository.save(score);
        recordHistory(score.getOrganization(), oldScore, newScore, reason, campaign, null);
    }

    @Transactional
    public void onEvidenceMissedDeadline(UUID campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));

        UUID organizationId = campaign.getOrganization().getId();
        TransparencyScore score = getScoreEntity(organizationId);

        String reason = "EVIDENCE_DEADLINE_MISSED";
        BigDecimal change = calculator.calculateChange(reason);
        BigDecimal newScore = calculator.calculateNewScore(score.getCurrentScore(), change);
        BigDecimal oldScore = score.getCurrentScore();

        score.setCurrentScore(newScore);
        score.setLastCalculatedAt(LocalDateTime.now());

        scoreRepository.save(score);
        recordHistory(score.getOrganization(), oldScore, newScore, reason, campaign, null);
    }

    @Transactional
    public void onReportUpheld(UUID reportId) {
        // Note: This method is called when a fraud report is confirmed
        // The reportId would link to a Report entity which has organization reference
        // For now, we assume the caller provides the organization ID context or
        // this will be integrated with a Report module in a future phase.
        // Placeholder implementation that accepts organizationId directly:
    }

    @Transactional
    public void onReportUpheldForOrganization(UUID organizationId, UUID reportId) {
        TransparencyScore score = getScoreEntity(organizationId);

        String reason = "REPORT_UPHELD";
        BigDecimal change = calculator.calculateChange(reason);
        BigDecimal newScore = calculator.calculateNewScore(score.getCurrentScore(), change);
        BigDecimal oldScore = score.getCurrentScore();

        score.setCurrentScore(newScore);
        score.setLastCalculatedAt(LocalDateTime.now());

        scoreRepository.save(score);
        recordHistory(score.getOrganization(), oldScore, newScore, reason, null, null);
    }

    @Transactional
    public void recalculateScore(UUID organizationId) {
        TransparencyScore score = getScoreEntity(organizationId);

        // Recalculation logic: Start from base 50 and apply all historical changes
        // For simplicity, we just update the lastCalculatedAt timestamp
        // A full recalculation would involve re-processing all evidence and campaign
        // data
        BigDecimal baseScore = new BigDecimal("50.00");

        // Calculate based on current stats
        int positivePoints = (score.getApprovedEvidences() * 5) +
                (score.getCompletedCampaigns() * 3);
        int negativePoints = (score.getRejectedEvidences() * 5);

        BigDecimal calculatedScore = baseScore
                .add(new BigDecimal(positivePoints))
                .subtract(new BigDecimal(negativePoints));

        BigDecimal newScore = calculator.calculateNewScore(BigDecimal.ZERO, calculatedScore);
        BigDecimal oldScore = score.getCurrentScore();

        score.setCurrentScore(newScore);
        score.setLastCalculatedAt(LocalDateTime.now());

        scoreRepository.save(score);
        recordHistory(score.getOrganization(), oldScore, newScore, "MANUAL_ADJUSTMENT", null, null);
    }

    public Page<TransparencyScoreResponse> getLowScoreOrganizations(Pageable pageable) {
        BigDecimal threshold = new BigDecimal("40.00");
        Page<TransparencyScore> lowScores = scoreRepository.findByCurrentScoreLessThan(threshold, pageable);
        return lowScores.map(this::mapToResponse);
    }

    public String getScoreLevel(BigDecimal score) {
        return calculator.getScoreLevel(score);
    }

    private TransparencyScore getScoreEntity(UUID organizationId) {
        return scoreRepository.findByOrganizationId(organizationId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Transparency score not found for organization " + organizationId));
    }

    private void recordHistory(Organization organization, BigDecimal prevScore, BigDecimal newScore, String reason,
            Campaign campaign, Evidence evidence) {
        TransparencyScoreHistory history = TransparencyScoreHistory.builder()
                .organization(organization)
                .previousScore(prevScore)
                .newScore(newScore)
                .changeReason(reason)
                .campaign(campaign)
                .evidence(evidence)
                .build();
        historyRepository.save(history);

        // Publish Event
        eventPublisher.publishEvent(new com.seffafbagis.api.event.TransparencyScoreChangedEvent(
                null, // triggeredBy (system action or unknown context in this helper)
                organization.getId(),
                prevScore,
                newScore,
                reason));
    }

    private TransparencyScoreResponse mapToResponse(TransparencyScore score) {
        return TransparencyScoreResponse.builder()
                .organizationId(score.getOrganization().getId())
                .organizationName(score.getOrganization().getLegalName())
                .currentScore(score.getCurrentScore())
                .scoreLevel(calculator.getScoreLevel(score.getCurrentScore()))
                .totalCampaigns(score.getTotalCampaigns())
                .completedCampaigns(score.getCompletedCampaigns())
                .onTimeReports(score.getOnTimeReports())
                .lateReports(score.getLateReports())
                .approvedEvidences(score.getApprovedEvidences())
                .rejectedEvidences(score.getRejectedEvidences())
                .lastCalculatedAt(score.getLastCalculatedAt())
                .build();
    }

    private ScoreHistoryResponse mapToHistoryResponse(TransparencyScoreHistory history) {
        BigDecimal change = history.getNewScore().subtract(
                history.getPreviousScore() != null ? history.getPreviousScore() : BigDecimal.ZERO);

        return ScoreHistoryResponse.builder()
                .id(history.getId())
                .previousScore(history.getPreviousScore())
                .newScore(history.getNewScore())
                .changeAmount(change)
                .changeReason(history.getChangeReason())
                .campaignTitle(history.getCampaign() != null ? history.getCampaign().getTitle() : null)
                .createdAt(history.getCreatedAt())
                .build();
    }

    @Transactional
    public void applyConsistencyBonus(UUID organizationId) {
        TransparencyScore score = getScoreEntity(organizationId);

        String reason = "CONSISTENCY_BONUS";
        BigDecimal bonus = new BigDecimal("1.00");
        BigDecimal newScore = calculator.calculateNewScore(score.getCurrentScore(), bonus);
        BigDecimal oldScore = score.getCurrentScore();

        score.setCurrentScore(newScore);
        score.setLastCalculatedAt(LocalDateTime.now());

        scoreRepository.save(score);
        recordHistory(score.getOrganization(), oldScore, newScore, reason, null, null);
    }
}
