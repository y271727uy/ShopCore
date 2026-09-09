package com.y271727uy.shopcore.integration.jei.sdmshop;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class SdmShopRuntimeBridge {
    private static volatile Object jeiRuntime;

    private SdmShopRuntimeBridge() {
    }

    @SuppressWarnings("unused")
    public static void setRuntime(Object runtime) {
        jeiRuntime = runtime;
    }

    @SuppressWarnings("unused")
    public static Object getRuntime() {
        return jeiRuntime;
    }

    public static boolean tryOpenCurrentScreen(ItemStack stack) {
        Objects.requireNonNull(stack, "stack");

        Minecraft minecraft = Minecraft.getInstance();
        Object screen = minecraft.screen;
        if (screen == null) {
            return false;
        }

        if (!isSdmShopScreen(screen)) {
            return false;
        }

        if (invokeScreenMethod(screen, stack)) {
            return true;
        }

        return showInJei(stack);
    }

    public static boolean openShop(SdmShopJeiEntry entry) {
        Objects.requireNonNull(entry, "entry");
        Object tab = SdmShopDataBridge.findShopTab(entry).orElseGet(() -> findClientShopTab(entry));
        if (!openSdmShopScreen()) {
            return false;
        }

        Object screen = Minecraft.getInstance().screen;
        if (screen == null || tab == null) {
            return true;
        }
        if (invokeSingleArgument(screen, "setSelectedTab", tab)) {
            invokeNoArgMethod(screen, "addEntriesButtons");
        }
        return true;
    }

    public static boolean showInJei(ItemStack stack) {
        Object runtime = jeiRuntime;
        if (runtime == null) {
            return false;
        }

        Object recipesGui = null;
        for (String methodName : List.of("getRecipesGui", "getRecipeGui", "getRecipeScreen")) {
            recipesGui = invokeNoArg(runtime, methodName).orElse(null);
            if (recipesGui != null) {
                break;
            }
        }
        if (recipesGui == null) {
            return false;
        }

        return invokeCompatible(recipesGui, stack)
                || invokeCompatible(recipesGui, List.of(stack))
                || invokeCompatible(recipesGui, List.of(stack.copy()));
    }

    private static boolean isSdmShopScreen(Object screen) {
        String className = screen.getClass().getName().toLowerCase();
        return className.contains("sdmshoprework")
                && (className.contains("shopscreen")
                || className.contains("buyer")
                || className.contains("legacy")
                || className.contains("modern")
                || className.contains("shoptab")
                || className.contains("abstractshoptab"));
    }

    private static boolean invokeScreenMethod(Object target, ItemStack stack) {
        return invokeCompatible(target, stack)
                || invokeCompatible(target, stack.getDescriptionId())
                || invokeCompatible(target, List.of(stack));
    }

    private static boolean openSdmShopScreen() {
        try {
            Class<?> clientClass = Class.forName("net.sixik.sdmshoprework.SDMShopClient");
            Method method = clientClass.getMethod("openGui", boolean.class);
            method.invoke(null, false);
            return true;
        } catch (ReflectiveOperationException | IllegalArgumentException ignored) {
            return false;
        }
    }

    private static Object findClientShopTab(SdmShopJeiEntry entry) {
        try {
            Class<?> shopBaseClass = Class.forName("net.sixik.sdmshoprework.common.shop.ShopBase");
            Field client = shopBaseClass.getField("CLIENT");
            Object shopBase = client.get(null);
            if (shopBase == null) {
                return null;
            }

            Object tabs = shopBaseClass.getMethod("getShopTabs").invoke(shopBase);
            if (!(tabs instanceof Iterable<?> iterable)) {
                return null;
            }
            for (Object tab : iterable) {
                String title = getTabTitle(tab);
                if (entry.shopName().equals(title)) {
                    return tab;
                }
            }
        } catch (ReflectiveOperationException | IllegalArgumentException ignored) {
        }
        return null;
    }

    private static String getTabTitle(Object tab) {
        try {
            Field title = tab.getClass().getField("title");
            Object value = title.get(tab);
            if (value instanceof Component component) {
                return component.getString();
            }
            return value == null ? "" : value.toString();
        } catch (ReflectiveOperationException ignored) {
            return "";
        }
    }

    private static boolean invokeSingleArgument(Object target, String methodName, Object argument) {
        for (Method method : target.getClass().getMethods()) {
            if (!method.getName().equals(methodName) || method.getParameterCount() != 1
                    || !method.getParameterTypes()[0].isInstance(argument)) {
                continue;
            }
            try {
                method.invoke(target, argument);
                return true;
            } catch (ReflectiveOperationException | IllegalArgumentException ignored) {
                return false;
            }
        }
        return false;
    }

    private static boolean invokeNoArgMethod(Object target, String methodName) {
        try {
            target.getClass().getMethod(methodName).invoke(target);
            return true;
        } catch (ReflectiveOperationException | IllegalArgumentException ignored) {
            return false;
        }
    }

    private static boolean invokeCompatible(Object target, Object argument) {
        for (Method method : target.getClass().getMethods()) {
            if (!isCandidateName(method.getName())) {
                continue;
            }
            if (!Modifier.isPublic(method.getModifiers())) {
                continue;
            }
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length != 1) {
                continue;
            }
            if (!isCompatible(parameterTypes[0], argument)) {
                continue;
            }
            try {
                method.invoke(target, argument);
                return true;
            } catch (ReflectiveOperationException | IllegalArgumentException ignored) {
            }
        }
        return false;
    }

    private static boolean isCandidateName(String name) {
        String normalized = name.toLowerCase();
        return normalized.contains("open")
                || normalized.contains("show")
                || normalized.contains("focus")
                || normalized.contains("jump")
                || normalized.contains("select")
                || normalized.contains("search");
    }

    private static boolean isCompatible(Class<?> parameterType, Object argument) {
        if (argument == null) {
            return !parameterType.isPrimitive();
        }
        if (parameterType.isInstance(argument) || parameterType.isAssignableFrom(argument.getClass())) {
            return true;
        }
        if (argument instanceof Collection<?> && Collection.class.isAssignableFrom(parameterType)) {
            return true;
        }
        return parameterType == Object.class;
    }

    private static Optional<Object> invokeNoArg(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            return Optional.ofNullable(method.invoke(target));
        } catch (ReflectiveOperationException ignored) {
            return Optional.empty();
        }
    }
}


