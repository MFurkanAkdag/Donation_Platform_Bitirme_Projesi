package com.seffafbagis.api.service.transparency;

import com.seffafbagis.api.entity.transparency.TransparencyScore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class TransparencyScoreCalculatorTest {

    @InjectMocks
    private TransparencyScoreCalculator calculator;

    @Test
    void calculateChange_ShouldReturnCorrectPoints() {
        assertEquals(new BigDecimal("5.00"), calculator.calculateChange("EVIDENCE_APPROVED_ON_TIME"));
        assertEquals(new BigDecimal("3.00"), calculator.calculateChange("EVIDENCE_APPROVED_LATE"));
        assertEquals(new BigDecimal("-5.00"), calculator.calculateChange("EVIDENCE_REJECTED"));
        assertEquals(new BigDecimal("-10.00"), calculator.calculateChange("EVIDENCE_DEADLINE_MISSED"));
        assertEquals(new BigDecimal("3.00"), calculator.calculateChange("CAMPAIGN_COMPLETED"));
        assertEquals(new BigDecimal("-2.00"), calculator.calculateChange("CAMPAIGN_CANCELLED"));
        assertEquals(BigDecimal.ZERO, calculator.calculateChange("UNKNOWN_REASON"));
    }

    @Test
    void calculateNewScore_ShouldEnforceUpperBound() {
        // 98 + 5 = 103 -> should be 100
        BigDecimal currentScore = new BigDecimal("98.00");
        BigDecimal change = new BigDecimal("5.00");

        BigDecimal newScore = calculator.calculateNewScore(currentScore, change);

        assertEquals(new BigDecimal("100.00"), newScore);
    }

    @Test
    void calculateNewScore_ShouldEnforceLowerBound() {
        // 3 - 5 = -2 -> should be 0
        BigDecimal currentScore = new BigDecimal("3.00");
        BigDecimal change = new BigDecimal("-5.00");

        BigDecimal newScore = calculator.calculateNewScore(currentScore, change);

        assertEquals(new BigDecimal("0.00"), newScore);
    }

    @Test
    void calculateNewScore_ShouldCalculateCorrectly() {
        // 50 + 5 = 55
        BigDecimal currentScore = new BigDecimal("50.00");
        BigDecimal change = new BigDecimal("5.00");

        BigDecimal newScore = calculator.calculateNewScore(currentScore, change);

        assertEquals(new BigDecimal("55.00"), newScore);
    }

    @Test
    void getScoreLevel_ShouldReturnCorrectLevels() {
        assertEquals("Çok Yüksek", calculator.getScoreLevel(new BigDecimal("85")));
        assertEquals("Yüksek", calculator.getScoreLevel(new BigDecimal("70")));
        assertEquals("Orta", calculator.getScoreLevel(new BigDecimal("50")));
        assertEquals("Düşük", calculator.getScoreLevel(new BigDecimal("35")));
        assertEquals("Çok Düşük", calculator.getScoreLevel(new BigDecimal("10")));
    }
}
