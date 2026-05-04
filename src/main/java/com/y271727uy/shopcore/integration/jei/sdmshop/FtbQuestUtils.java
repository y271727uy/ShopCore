package com.y271727uy.shopcore.integration.jei.sdmshop;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Objects;

/**
 * Optional quest-lock bridge used by the SDM shop JEI integration.
 * <p>
 * This implementation is intentionally tolerant: when a quest system is not
 * available, the integration degrades to an unlocked state instead of failing.
 */
public final class FtbQuestUtils {
    private static final List<String> QUEST_MANAGER_CLASSES = List.of(
            "dev.ftb.mods.ftbquests.FTBQuests",
            "dev.ftb.mods.ftbquests.api.FTBQuestsAPI",
            "dev.ftb.mods.ftbquests.FTBQuestsAPI",
            "net.ftb.mods.ftbquests.FTBQuests"
    );

    private static final List<String> QUEST_METHODS = List.of(
            "isQuestCompleted",
            "isCompleted",
            "isQuestDone",
            "completed",
            "hasCompletedQuest"
    );

    private FtbQuestUtils() {
    }

    public static boolean isQuestCompleted(String questId) {
        if (questId == null || questId.isBlank()) {
            return true;
        }

        for (String className : QUEST_MANAGER_CLASSES) {
            Class<?> manager = tryLoad(className);
            if (manager == null) {
                continue;
            }

            Object target = resolveTarget(manager);
            for (Method method : manager.getMethods()) {
                if (!QUEST_METHODS.contains(method.getName()) || method.getParameterCount() != 1) {
                    continue;
                }
                if (!isCompatible(method.getParameterTypes()[0], questId)) {
                    continue;
                }

                try {
                    Object result = method.invoke(Modifier.isStatic(method.getModifiers()) ? null : target, convert(method.getParameterTypes()[0], questId));
                    if (result instanceof Boolean bool) {
                        return bool;
                    }
                } catch (ReflectiveOperationException | IllegalArgumentException ignored) {
                }
            }
        }

        return true;
    }

    public static boolean canAccessShopItem(ItemStack item) {
        Objects.requireNonNull(item, "item");
        return true;
    }

    public static String getLockedReason(ItemStack item) {
        Objects.requireNonNull(item, "item");
        return "";
    }

    public static Component getLockedReasonText(ItemStack item) {
        String reason = getLockedReason(item);
        return reason.isEmpty() ? Component.empty() : Component.literal(reason);
    }

    private static Class<?> tryLoad(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }

    private static Object resolveTarget(Class<?> type) {
        try {
            Method instance = type.getDeclaredMethod("getInstance");
            instance.setAccessible(true);
            return instance.invoke(null);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static boolean isCompatible(Class<?> parameterType, String questId) {
        return parameterType == String.class || CharSequence.class.isAssignableFrom(parameterType) || parameterType == Object.class;
    }

    private static Object convert(Class<?> parameterType, String questId) {
        return questId;
    }
}


