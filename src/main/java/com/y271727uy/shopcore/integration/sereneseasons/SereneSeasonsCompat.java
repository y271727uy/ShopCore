package com.y271727uy.shopcore.integration.sereneseasons;

import net.minecraft.world.level.Level;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Optional;

public final class SereneSeasonsCompat {
    private static final String[] HELPER_CLASS_NAMES = {
            "sereneseasons.api.season.SeasonHelper",
            "sereneseasons.api.season.SeasonsHelper"
    };

    private static final String[] STATE_METHOD_NAMES = {
            "getSeasonState",
            "getSeasonStateForLevel",
            "getSeasonStateAt"
    };

    private static final String[] SEASON_METHOD_NAMES = {
            "getSeason",
            "getSubSeason",
            "season",
            "subSeason"
    };

    private SereneSeasonsCompat() {
    }

    public static Optional<String> getCurrentSeasonId(Level level) {
        if (level == null) {
            return Optional.empty();
        }

        for (String helperClassName : HELPER_CLASS_NAMES) {
            Class<?> helperClass = findClass(helperClassName);
            if (helperClass == null) {
                continue;
            }

            Object seasonState = invokeStaticNoArgOrLevel(helperClass, level, STATE_METHOD_NAMES);
            if (seasonState == null) {
                continue;
            }

            Optional<String> currentSeason = extractSeasonId(seasonState);
            if (currentSeason.isPresent()) {
                return currentSeason;
            }
        }

        return Optional.empty();
    }

    private static Optional<String> extractSeasonId(Object seasonState) {
        for (String methodName : SEASON_METHOD_NAMES) {
            Object season = invokeNoArg(seasonState, methodName);
            if (season == null) {
                continue;
            }

            Optional<String> normalized = normalizeSeasonId(season.toString());
            if (normalized.isPresent()) {
                return normalized;
            }
        }

        return normalizeSeasonId(seasonState.toString());
    }

    private static Object invokeStaticNoArgOrLevel(Class<?> type, Level level, String[] methodNames) {
        for (String methodName : methodNames) {
            for (Method method : type.getMethods()) {
                if (!method.getName().equals(methodName) || method.getParameterCount() != 1) {
                    continue;
                }

                Class<?> parameterType = method.getParameterTypes()[0];
                if (!parameterType.isInstance(level) && !parameterType.isAssignableFrom(level.getClass())) {
                    continue;
                }

                try {
                    return method.invoke(null, level);
                } catch (ReflectiveOperationException ignored) {
                    continue;
                }
            }
        }

        return null;
    }

    private static Object invokeNoArg(Object target, String methodName) {
        for (Method method : target.getClass().getMethods()) {
            if (!method.getName().equals(methodName) || method.getParameterCount() != 0) {
                continue;
            }

            try {
                return method.invoke(target);
            } catch (ReflectiveOperationException ignored) {
                continue;
            }
        }

        return null;
    }

    private static Class<?> findClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }

    private static Optional<String> normalizeSeasonId(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return Optional.empty();
        }

        String normalized = rawValue.trim().toLowerCase(Locale.ROOT);
        if (normalized.contains("spring")) {
            return Optional.of("spring");
        }
        if (normalized.contains("summer")) {
            return Optional.of("summer");
        }
        if (normalized.contains("autumn") || normalized.contains("fall")) {
            return Optional.of("autumn");
        }
        if (normalized.contains("winter")) {
            return Optional.of("winter");
        }

        return Optional.of(normalized);
    }
}
