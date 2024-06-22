package com.github.natche.cyderutils.strings;

import com.google.common.base.Preconditions;
import com.github.natche.cyderutils.exceptions.IllegalMethodException;
import com.github.natche.cyderutils.math.NumberUtil;

/**
 * Utilities related to the Levenshtein distance of strings.
 */
public final class LevenshteinUtil {
    /**
     * Suppress default constructor.
     *
     * @throws IllegalMethodException if invoked
     */
    private LevenshteinUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Returns the levenshtein distance between string alpha and string beta.
     * From <a href="http://rosettacode.org/wiki/Levenshtein_distance#Iterative_space_optimized_.28even_bounded.29</a>
     *
     * @param alpha the first string
     * @param beta  the second string
     * @return the levenshtein distance between alpha and beta
     */
    public static int computeLevenshteinDistance(String alpha, String beta) {
        Preconditions.checkNotNull(alpha);
        Preconditions.checkNotNull(beta);

        // Simplest case
        if (alpha.equals(beta)) return 0;

        int lengthAlpha = alpha.length();
        int lengthBeta = beta.length();

        // If one is empty, the distance is the length of the other
        if (lengthAlpha == 0) return lengthBeta;
        if (lengthBeta == 0) return lengthAlpha;

        if (lengthAlpha < lengthBeta) {
            // Swap lengths
            int tempAlphaLength = lengthAlpha;
            lengthAlpha = lengthBeta;
            lengthBeta = tempAlphaLength;

            // Swap strings
            String tempAlphaString = alpha;
            alpha = beta;
            beta = tempAlphaString;
        }

        // Initialize cost array
        int[] cost = new int[lengthBeta + 1];
        for (int i = 0 ; i <= lengthBeta ; i += 1) {
            cost[i] = i;
        }

        for (int i = 1 ; i <= lengthAlpha ; i += 1) {
            cost[0] = i;
            int previous = i - 1;
            // minimum cost for next generation
            int min = previous;

            for (int j = 1 ; j <= lengthBeta ; j += 1) {
                // cost of alpha[0, i] to beta[0, j], same char is 0, different is +1 cost
                int act = previous + (alpha.charAt(i - 1) == beta.charAt(j - 1) ? 0 : 1);
                cost[j] = NumberUtil.min(1 + (previous = cost[j]), 1 + cost[j - 1], act);

                if (previous < min) {
                    min = previous;
                }
            }
        }

        return cost[lengthBeta];
    }
}
