package com.y271727uy.shopcore.integration.sdm_integration;

import com.y271727uy.shopcore.economic.CurrencyOperationResult;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.UUID;

/**
 * Reflection-based adapter around {@code net.sixik.sdm_economy.api.CurrencyHelper}.
 * <p>
 * The SDM economy dependency is provided by the modpack, so this adapter keeps
 * ShopCore compile-safe even when the exact helper signature changes between releases.
 */
public final class SdmCurrencyHelperBridge {
    private static final String HELPER_CLASS_NAME = "net.sixik.sdm_economy.api.CurrencyHelper";
    private static final String DEFAULT_REASON = "shopcore";

    private static final List<String> BALANCE_METHOD_NAMES = List.of(
            "getBalance",
            "getPlayerBalance",
            "getMoney",
            "getCurrency",
            "balance"
    );

    private static final List<String> INCREASE_METHOD_NAMES = List.of(
            "addCurrency",
            "addMoney",
            "giveCurrency",
            "deposit",
            "increaseCurrency",
            "increaseMoney"
    );

    private static final List<String> DECREASE_METHOD_NAMES = List.of(
            "removeCurrency",
            "removeMoney",
            "takeCurrency",
            "withdraw",
            "decreaseCurrency",
            "decreaseMoney",
            "deductCurrency",
            "deductMoney",
            "subtractCurrency"
    );

    private static volatile Class<?> helperClass;
    private static volatile Object helperInstance;

    private SdmCurrencyHelperBridge() {
    }

    public static boolean isAvailable() {
        return resolveHelperClass() != null;
    }

    public static CurrencyOperationResult increase(Player player, double amount) {
        return adjust(player, player.getUUID(), Math.abs(amount));
    }

    public static CurrencyOperationResult decrease(Player player, double amount) {
        return adjust(player, player.getUUID(), -Math.abs(amount));
    }

    public static CurrencyOperationResult adjust(Player player, double delta) {
        return adjust(player, player.getUUID(), delta);
    }

    @SuppressWarnings("unused")
    public static CurrencyOperationResult increase(UUID accountUuid, double amount) {
        return adjust(null, Objects.requireNonNull(accountUuid, "accountUuid"), Math.abs(amount));
    }

    @SuppressWarnings("unused")
    public static CurrencyOperationResult decrease(UUID accountUuid, double amount) {
        return adjust(null, Objects.requireNonNull(accountUuid, "accountUuid"), -Math.abs(amount));
    }

    @SuppressWarnings("unused")
    public static CurrencyOperationResult adjust(UUID accountUuid, double delta) {
        return adjust(null, Objects.requireNonNull(accountUuid, "accountUuid"), delta);
    }

    @SuppressWarnings("unused")
    public static OptionalDouble queryBalance(UUID accountUuid) {
        return queryBalance(null, Objects.requireNonNull(accountUuid, "accountUuid"));
    }

    private static CurrencyOperationResult adjust(Player player, UUID accountUuid, double delta) {
        if (!Double.isFinite(delta)) {
            return CurrencyOperationResult.failure(delta, "currency amount must be finite");
        }
        if (delta == 0D) {
            return CurrencyOperationResult.success(0D, queryBalance(player, accountUuid), "no-op currency change");
        }

        Class<?> helper = resolveHelperClass();
        if (helper == null) {
            return CurrencyOperationResult.failure(delta, "sdm-economy CurrencyHelper is not available");
        }

        double amount = Math.abs(delta);
        List<String> methodNames = delta > 0 ? INCREASE_METHOD_NAMES : DECREASE_METHOD_NAMES;
        InvocationOutcome outcome = invokeOperation(helper, methodNames, player, accountUuid, amount);
        if (!outcome.success()) {
            return CurrencyOperationResult.failure(delta, outcome.message());
        }

        return CurrencyOperationResult.success(delta, queryBalance(player, accountUuid), outcome.message());
    }

    public static OptionalDouble queryBalance(Player player) {
        return queryBalance(player, player.getUUID());
    }

    private static OptionalDouble queryBalance(Player player, UUID accountUuid) {
        if (player == null && accountUuid == null) {
            return OptionalDouble.empty();
        }
        Class<?> helper = resolveHelperClass();
        if (helper == null) {
            return OptionalDouble.empty();
        }

        InvocationOutcome outcome = invokeQuery(helper, player, accountUuid);
        if (!outcome.success() || !(outcome.value() instanceof Number number)) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(number.doubleValue());
    }

    private static Class<?> resolveHelperClass() {
        Class<?> resolved = helperClass;
        if (resolved != null) {
            return resolved;
        }

        synchronized (SdmCurrencyHelperBridge.class) {
            if (helperClass != null) {
                return helperClass;
            }
            try {
                helperClass = Class.forName(HELPER_CLASS_NAME);
            } catch (ClassNotFoundException ignored) {
                helperClass = null;
            }
            return helperClass;
        }
    }

    private static Object resolveHelperTarget(Class<?> helper) {
        if (helper == null) {
            return null;
        }

        if (helperInstance != null) {
            return helperInstance;
        }

        synchronized (SdmCurrencyHelperBridge.class) {
            if (helperInstance != null) {
                return helperInstance;
            }
            try {
                helperInstance = helper.getDeclaredConstructor().newInstance();
            } catch (ReflectiveOperationException ignored) {
                helperInstance = null;
            }
            return helperInstance;
        }
    }

    private static InvocationOutcome invokeOperation(Class<?> helper, List<String> methodNames, Player player, UUID accountUuid, double amount) {
        Method method = findCandidateMethod(helper, methodNames, candidate -> candidate.length >= 2 && candidate.length <= 3, player, accountUuid, amount);
        if (method == null) {
            return InvocationOutcome.failure("no compatible CurrencyHelper operation method was found");
        }

        Object target = Modifier.isStatic(method.getModifiers()) ? null : resolveHelperTarget(helper);
        if (!Modifier.isStatic(method.getModifiers()) && target == null) {
            return InvocationOutcome.failure("CurrencyHelper instance could not be created");
        }

        try {
            Object[] arguments = buildArguments(method, player, accountUuid, amount);
            if (arguments == null) {
                return InvocationOutcome.failure("CurrencyHelper method signature is not supported: " + method);
            }

            Object value = method.invoke(target, arguments);
            if (value instanceof Boolean bool && !bool) {
                return InvocationOutcome.failure(method.getName() + " returned false");
            }
            return InvocationOutcome.success(method.getName() + " invoked successfully", value);
        } catch (ReflectiveOperationException | IllegalArgumentException exception) {
            return InvocationOutcome.failure(exception.getClass().getSimpleName() + ": " + exception.getMessage());
        }
    }

    private static InvocationOutcome invokeQuery(Class<?> helper, Player player, UUID accountUuid) {
        Method method = findCandidateMethod(helper, BALANCE_METHOD_NAMES, candidate -> candidate.length == 1, player, accountUuid, 0D);
        if (method == null) {
            return InvocationOutcome.failure("no compatible CurrencyHelper balance method was found");
        }

        Object target = Modifier.isStatic(method.getModifiers()) ? null : resolveHelperTarget(helper);
        if (!Modifier.isStatic(method.getModifiers()) && target == null) {
            return InvocationOutcome.failure("CurrencyHelper instance could not be created");
        }

        try {
            Object[] arguments = buildArguments(method, player, accountUuid, 0D);
            if (arguments == null) {
                return InvocationOutcome.failure("CurrencyHelper balance method signature is not supported: " + method);
            }

            Object value = method.invoke(target, arguments);
            return InvocationOutcome.success(method.getName() + " invoked successfully", value);
        } catch (ReflectiveOperationException | IllegalArgumentException exception) {
            return InvocationOutcome.failure(exception.getClass().getSimpleName() + ": " + exception.getMessage());
        }
    }

    private static Method findCandidateMethod(Class<?> helper, List<String> methodNames, java.util.function.Predicate<Class<?>[]> signaturePredicate, Player player, UUID accountUuid, double amount) {
        for (Method method : helper.getMethods()) {
            if (!methodNames.contains(method.getName())) {
                continue;
            }
            if (!signaturePredicate.test(method.getParameterTypes())) {
                continue;
            }
            if (buildArguments(method, player, accountUuid, amount) != null) {
                return method;
            }
        }
        return null;
    }

    private static Object[] buildArguments(Method method, Player player, UUID accountUuid, double amount) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] arguments = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            Object value = null;

            if (isPlayerType(parameterType, player)) {
                value = player;
            } else if (isUuidType(parameterType, accountUuid)) {
                value = accountUuid;
            } else if (isStringType(parameterType)) {
                value = DEFAULT_REASON;
            } else if (isBooleanType(parameterType)) {
                value = Boolean.TRUE;
            } else if (isAmountType(parameterType)) {
                value = convertAmount(parameterType, amount);
            }

            if (value == null) {
                return null;
            }
            arguments[i] = value;
        }

        return arguments;
    }

    private static boolean isPlayerType(Class<?> parameterType, Player player) {
        return player != null && (parameterType.isInstance(player) || parameterType.isAssignableFrom(player.getClass()));
    }

    private static boolean isUuidType(Class<?> parameterType, UUID accountUuid) {
        return accountUuid != null && UUID.class.isAssignableFrom(parameterType);
    }

    private static boolean isStringType(Class<?> parameterType) {
        return parameterType == String.class || CharSequence.class.isAssignableFrom(parameterType) || parameterType == Object.class;
    }

    private static boolean isBooleanType(Class<?> parameterType) {
        return parameterType == boolean.class || parameterType == Boolean.class;
    }

    private static boolean isAmountType(Class<?> parameterType) {
        return parameterType == double.class
                || parameterType == Double.class
                || parameterType == float.class
                || parameterType == Float.class
                || parameterType == long.class
                || parameterType == Long.class
                || parameterType == int.class
                || parameterType == Integer.class
                || parameterType == short.class
                || parameterType == Short.class
                || parameterType == byte.class
                || parameterType == Byte.class
                || Number.class.isAssignableFrom(parameterType)
                || parameterType == BigDecimal.class;
    }

    private static Object convertAmount(Class<?> parameterType, double amount) {
        if (parameterType == double.class || parameterType == Double.class) {
            return amount;
        }
        if (parameterType == float.class || parameterType == Float.class) {
            return (float) amount;
        }
        if (parameterType == long.class || parameterType == Long.class) {
            return Math.round(amount);
        }
        if (parameterType == int.class || parameterType == Integer.class) {
            return (int) Math.round(amount);
        }
        if (parameterType == short.class || parameterType == Short.class) {
            return (short) Math.round(amount);
        }
        if (parameterType == byte.class || parameterType == Byte.class) {
            return (byte) Math.round(amount);
        }
        if (parameterType == BigDecimal.class) {
            return BigDecimal.valueOf(amount);
        }
        return amount;
    }

    private record InvocationOutcome(boolean success, String message, Object value) {
        private static InvocationOutcome success(String message, Object value) {
            return new InvocationOutcome(true, message, value);
        }

        private static InvocationOutcome failure(String message) {
            return new InvocationOutcome(false, message, null);
        }
    }
}



