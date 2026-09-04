package logic;

import model.Difficulty;
import model.LevelResult;

/**
 * StarsCalculator.java
 *
 * Calculates the 1-3 star rating for a completed level based on:
 * - Time efficiency (relative to the time limit)
 * - Mistakes made
 * - Hint usage
 *
 * 3 stars: Fast + accurate + efficient
 * 2 stars: Completed with some mistakes
 * 1 star:  Barely completed
 */
public class StarsCalculator {

    private static final double STAR3_TIME_FACTOR = 0.6;   // finish within 60% of limit
    private static final double STAR3_MAX_MISTAKES = 1;
    private static final double STAR2_TIME_FACTOR = 0.9;   // finish within 90% of limit
    private static final double STAR2_MAX_MISTAKES = 4;

    private StarsCalculator() {
    }

    /**
     * Calculates stars from 1 to 3 based on performance metrics.
     */
    public static int calculateStars(int timeUsedSeconds, int timeLimitSeconds,
                                     int mistakes, int hintsUsed) {
        // Safety: never below 1 star if the level was completed
        int stars = 1;

        double timeFactor = timeLimitSeconds > 0
                ? (double) timeUsedSeconds / timeLimitSeconds
                : 1.0;

        boolean goodTime = timeFactor <= STAR3_TIME_FACTOR;
        boolean okTime = timeFactor <= STAR2_TIME_FACTOR;
        boolean fewMistakes = mistakes <= STAR3_MAX_MISTAKES;
        boolean someMistakes = mistakes <= STAR2_MAX_MISTAKES;
        boolean fewHints = hintsUsed <= 1;

        if (goodTime && fewMistakes && fewHints) {
            stars = 3;
        } else if (okTime && someMistakes) {
            stars = 2;
        } else {
            stars = 1;
        }

        return stars;
    }

    /**
     * Calculates efficiency percentage (how cleanly the player solved the level).
     * Mistakes, hints, and slow time reduce efficiency.
     */
    public static double calculateEfficiency(int timeUsedSeconds, int timeLimitSeconds,
                                             int mistakes, int hintsUsed) {
        double base = 100.0;

        double timePenalty = 0.0;
        if (timeLimitSeconds > 0) {
            double timeFactor = (double) timeUsedSeconds / timeLimitSeconds;
            if (timeFactor > 0.8) {
                timePenalty = (timeFactor - 0.8) * 40.0;
            }
        }

        double mistakePenalty = Math.min(mistakes * 8.0, 40.0);
        double hintPenalty = Math.min(hintsUsed * 10.0, 30.0);

        double efficiency = base - timePenalty - mistakePenalty - hintPenalty;
        return Math.max(0, Math.min(100, efficiency));
    }
}