package com.y271727uy.shopcore.core.algorithm.shapleyvalue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public final class ShapleyValue {
    public static final int DEFAULT_MAX_EXACT_PARTICIPANTS = 20;
    public static final int HARD_MAX_EXACT_PARTICIPANTS = 24;

    private ShapleyValue() {
    }

    public static <P> ShapleyValueResult<P> exact(List<P> participants, CoalitionValueFunction<P> valueFunction) {
        return exact(participants, valueFunction, DEFAULT_MAX_EXACT_PARTICIPANTS);
    }

    public static <P> ShapleyValueResult<P> calculate(
            List<P> participants,
            CoalitionValueFunction<P> valueFunction,
            ShapleyValueMethod method,
            int monteCarloIterations,
            Random random
    ) {
        Objects.requireNonNull(method, "method");
        return switch (method) {
            case EXACT -> exact(participants, valueFunction);
            case MONTE_CARLO -> monteCarlo(participants, valueFunction, monteCarloIterations, random);
        };
    }

    public static <P> ShapleyValueResult<P> exact(
            List<P> participants,
            CoalitionValueFunction<P> valueFunction,
            int maxParticipants
    ) {
        List<P> normalizedParticipants = normalizeParticipants(participants);
        Objects.requireNonNull(valueFunction, "valueFunction");
        if (maxParticipants < 0) {
            throw new IllegalArgumentException("maxParticipants must be non-negative");
        }
        int participantCount = normalizedParticipants.size();
        if (participantCount > maxParticipants) {
            throw new IllegalArgumentException("Exact Shapley value requires at most " + maxParticipants
                    + " participants, got " + participantCount);
        }
        if (participantCount > HARD_MAX_EXACT_PARTICIPANTS) {
            throw new IllegalArgumentException("Exact Shapley value currently supports at most "
                    + HARD_MAX_EXACT_PARTICIPANTS + " participants");
        }

        int coalitionCount = 1 << participantCount;
        double[] coalitionValues = new double[coalitionCount];
        for (int mask = 0; mask < coalitionCount; mask++) {
            coalitionValues[mask] = valueFunction.valueOf(toCoalition(normalizedParticipants, mask));
        }

        double[] weights = buildExactWeights(participantCount);
        Map<P, Double> shares = new LinkedHashMap<>();
        for (int participantIndex = 0; participantIndex < participantCount; participantIndex++) {
            int participantBit = 1 << participantIndex;
            double share = 0.0D;
            for (int mask = 0; mask < coalitionCount; mask++) {
                if ((mask & participantBit) != 0) {
                    continue;
                }

                int coalitionSize = Integer.bitCount(mask);
                int withParticipantMask = mask | participantBit;
                double marginalContribution = coalitionValues[withParticipantMask] - coalitionValues[mask];
                share += weights[coalitionSize] * marginalContribution;
            }
            shares.put(normalizedParticipants.get(participantIndex), share);
        }

        return new ShapleyValueResult<>(
                shares,
                coalitionValues[0],
                coalitionValues[coalitionCount - 1],
                ShapleyValueMethod.EXACT,
                coalitionCount
        );
    }

    public static <P> ShapleyValueResult<P> monteCarlo(
            List<P> participants,
            CoalitionValueFunction<P> valueFunction,
            int iterations,
            Random random
    ) {
        List<P> normalizedParticipants = normalizeParticipants(participants);
        Objects.requireNonNull(valueFunction, "valueFunction");
        Objects.requireNonNull(random, "random");
        if (iterations <= 0) {
            throw new IllegalArgumentException("iterations must be positive");
        }

        Map<P, Double> shares = new LinkedHashMap<>();
        for (P participant : normalizedParticipants) {
            shares.put(participant, 0.0D);
        }

        List<P> permutation = new ArrayList<>(normalizedParticipants);
        for (int iteration = 0; iteration < iterations; iteration++) {
            Collections.shuffle(permutation, random);

            Set<P> coalition = new LinkedHashSet<>();
            double previousValue = valueFunction.valueOf(Collections.unmodifiableSet(coalition));
            for (P participant : permutation) {
                coalition.add(participant);
                double nextValue = valueFunction.valueOf(Collections.unmodifiableSet(coalition));
                shares.put(participant, shares.get(participant) + nextValue - previousValue);
                previousValue = nextValue;
            }
        }

        for (Map.Entry<P, Double> entry : shares.entrySet()) {
            entry.setValue(entry.getValue() / iterations);
        }

        double emptyCoalitionValue = valueFunction.valueOf(Set.of());
        double grandCoalitionValue = valueFunction.valueOf(Collections.unmodifiableSet(new LinkedHashSet<>(normalizedParticipants)));
        return new ShapleyValueResult<>(
                shares,
                emptyCoalitionValue,
                grandCoalitionValue,
                ShapleyValueMethod.MONTE_CARLO,
                iterations
        );
    }

    private static <P> List<P> normalizeParticipants(List<P> participants) {
        Objects.requireNonNull(participants, "participants");
        List<P> normalizedParticipants = List.copyOf(participants);
        Set<P> uniqueParticipants = new LinkedHashSet<>(normalizedParticipants);
        if (uniqueParticipants.size() != normalizedParticipants.size()) {
            throw new IllegalArgumentException("participants must not contain duplicates");
        }
        return normalizedParticipants;
    }

    private static double[] buildExactWeights(int participantCount) {
        double[] weights = new double[participantCount];
        if (participantCount == 0) {
            return weights;
        }

        double[] factorials = new double[participantCount + 1];
        factorials[0] = 1.0D;
        for (int i = 1; i < factorials.length; i++) {
            factorials[i] = factorials[i - 1] * i;
        }

        double divisor = factorials[participantCount];
        for (int coalitionSize = 0; coalitionSize < participantCount; coalitionSize++) {
            weights[coalitionSize] = factorials[coalitionSize]
                    * factorials[participantCount - coalitionSize - 1]
                    / divisor;
        }
        return weights;
    }

    private static <P> Set<P> toCoalition(List<P> participants, int mask) {
        Set<P> coalition = new LinkedHashSet<>();
        for (int i = 0; i < participants.size(); i++) {
            if ((mask & (1 << i)) != 0) {
                coalition.add(participants.get(i));
            }
        }
        return Collections.unmodifiableSet(coalition);
    }
}
