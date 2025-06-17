package com.uni.ethesis.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class GradingUtil {

    // --- Configuration Constants ---
    // Scores
    private static final int MINIMUM_POSSIBLE_SCORE = 0;
    private static final int MAXIMUM_POSSIBLE_SCORE = 100;
    private static final int MINIMUM_PASSING_SCORE_INT = 50; // Integer score threshold

    // Grades (using String constructor for BigDecimal for precision)
    private static final BigDecimal FAIL_GRADE = new BigDecimal("2.0");
    private static final BigDecimal LOWEST_PASS_GRADE = new BigDecimal("3.0");
    private static final BigDecimal HIGHEST_GRADE = new BigDecimal("6.0");
    private static final BigDecimal GRADE_INCREMENT = new BigDecimal("0.5");

    // BigDecimal constants for calculation
    private static final BigDecimal BD_MINIMUM_PASSING_SCORE = new BigDecimal(String.valueOf(MINIMUM_PASSING_SCORE_INT));
    private static final BigDecimal BD_MAXIMUM_POSSIBLE_SCORE = new BigDecimal(String.valueOf(MAXIMUM_POSSIBLE_SCORE));
    private static final BigDecimal TWO = new BigDecimal("2.0"); // For rounding to nearest 0.5

    // Scale for intermediate division operations to maintain precision
    private static final int CALCULATION_SCALE = 10; // Number of decimal places for intermediate calculations

    /**
     * Calculates a grade with 0.5 increments (e.g., 2.0, 2.5, ..., 6.0)
     * based on a 100-point test score.
     *
     * @param score The student's score (0-100).
     * @return The calculated BigDecimal grade.
     */
    public static BigDecimal calculateGrade(int score) {
        // 1. Clamp score to be within 0-100
        if (score < MINIMUM_POSSIBLE_SCORE) {
            score = MINIMUM_POSSIBLE_SCORE;
        }
        if (score > MAXIMUM_POSSIBLE_SCORE) {
            score = MAXIMUM_POSSIBLE_SCORE;
        }

        // 2. Handle Fail Case
        if (score < MINIMUM_PASSING_SCORE_INT) {
            return FAIL_GRADE; // e.g., 2.0
        }

        // 3. Handle Passing Case (scores >= MINIMUM_PASSING_SCORE_INT)
        BigDecimal bdScore = new BigDecimal(String.valueOf(score));

        // Effective range of scores that map to passing grades
        // e.g., 100 - 50 = 50
        BigDecimal passingScoreSpan = BD_MAXIMUM_POSSIBLE_SCORE.subtract(BD_MINIMUM_PASSING_SCORE);

        // Effective range of passing grades
        // e.g., 6.0 - 3.0 = 3.0
        BigDecimal passingGradeSpan = HIGHEST_GRADE.subtract(LOWEST_PASS_GRADE);

        // If passingScoreSpan is zero (e.g., MPS = 100), and score is MPS, return highest grade.
        // Avoid division by zero.
        if (passingScoreSpan.compareTo(BigDecimal.ZERO) == 0) {
            if (bdScore.compareTo(BD_MINIMUM_PASSING_SCORE) >= 0) {
                return HIGHEST_GRADE;
            } else { // Should have been caught by fail case, but for safety
                return FAIL_GRADE;
            }
        }

        // Calculate the raw, precise grade using linear interpolation
        // Formula: lowestPass + ((score - mps) / (maxScore - mps)) * (highestPass - lowestPass)
        BigDecimal scoreAboveMinPass = bdScore.subtract(BD_MINIMUM_PASSING_SCORE);
        BigDecimal proportion = scoreAboveMinPass.divide(passingScoreSpan, CALCULATION_SCALE, RoundingMode.HALF_UP);
        BigDecimal gradeOffset = proportion.multiply(passingGradeSpan);
        BigDecimal rawCalculatedGrade = LOWEST_PASS_GRADE.add(gradeOffset);

        // 4. Round the rawCalculatedGrade to the nearest 0.5
        // Method: Multiply by 2, round to whole number, divide by 2
        BigDecimal multipliedByTwo = rawCalculatedGrade.multiply(TWO);
        BigDecimal roundedToWhole = multipliedByTwo.setScale(0, RoundingMode.HALF_UP); // Round to 0 decimal places
        BigDecimal finalGrade = roundedToWhole.divide(TWO, 1, RoundingMode.HALF_UP); // Divide by 2, keep 1 decimal place (e.g., X.0 or X.5)


        // 5. Clamp the final grade to ensure it's within the defined pass range
        //    (mostly for edge cases or if rounding pushes it slightly out)
        if (finalGrade.compareTo(HIGHEST_GRADE) > 0) {
            finalGrade = HIGHEST_GRADE;
        }
        // This check ensures that if rawCalculatedGrade was slightly below LOWEST_PASS_GRADE
        // after calculations but was for a passing score, it gets corrected.
        // Or if MPS exactly maps to LOWEST_PASS_GRADE.
        if (finalGrade.compareTo(LOWEST_PASS_GRADE) < 0) {
            finalGrade = LOWEST_PASS_GRADE;
        }


        return finalGrade;
    }
}
