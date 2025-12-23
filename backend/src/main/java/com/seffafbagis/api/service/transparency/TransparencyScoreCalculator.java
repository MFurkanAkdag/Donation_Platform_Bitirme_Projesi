package com.seffafbagis.api.service.transparency;

import com.seffafbagis.api.entity.transparency.TransparencyScore;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TransparencyScoreCalculator {

    public BigDecimal calculateChange(String reason) {
        return switch (reason) {
            case "EVIDENCE_APPROVED_ON_TIME" -> new BigDecimal("5.00");
            case "EVIDENCE_APPROVED_LATE" -> new BigDecimal("3.00");
            case "EVIDENCE_APPROVED_AFTER_DEADLINE" -> new BigDecimal("2.00");
            case "CAMPAIGN_COMPLETED" -> new BigDecimal("3.00");
            case "MONTHLY_CONSISTENCY_BONUS" -> new BigDecimal("1.00");
            case "EVIDENCE_REJECTED" -> new BigDecimal("-5.00");
            case "EVIDENCE_DEADLINE_MISSED" -> new BigDecimal("-10.00");
            case "EVIDENCE_LATE_UPLOAD" -> new BigDecimal("-3.00");
            case "CAMPAIGN_CANCELLED" -> new BigDecimal("-2.00");
            case "REPORT_UPHELD" -> new BigDecimal("-15.00");
            case "INITIAL_SCORE" -> new BigDecimal("50.00");
            default -> BigDecimal.ZERO;
        };
    }

    public BigDecimal calculateNewScore(BigDecimal currentScore, BigDecimal change) {
        BigDecimal newScore = currentScore.add(change);

        // Ensure bounds 0 - 100
        if (newScore.compareTo(new BigDecimal("0.00")) < 0) {
            return new BigDecimal("0.00");
        }
        if (newScore.compareTo(new BigDecimal("100.00")) > 0) {
            return new BigDecimal("100.00");
        }

        return newScore;
    }

    public String getScoreLevel(BigDecimal score) {
        if (score.compareTo(new BigDecimal("80.00")) >= 0) {
            return "Çok Yüksek";
        } else if (score.compareTo(new BigDecimal("60.00")) >= 0) {
            return "Yüksek";
        } else if (score.compareTo(new BigDecimal("40.00")) >= 0) {
            return "Orta";
        } else if (score.compareTo(new BigDecimal("30.00")) >= 0) {
            return "Düşük";
        } else {
            return "Çok Düşük";
        }
    }
}
