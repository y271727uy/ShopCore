package com.y271727uy.shopcore.integration.jei.sdmshop;

import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class SdmShopRuntimeBridge {
    private static volatile IJeiRuntime jeiRuntime;

    private SdmShopRuntimeBridge() {
    }

    public static void setRuntime(IJeiRuntime runtime) {
        jeiRuntime = runtime;
    }

    public static IJeiRuntime getRuntime() {
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

    public static boolean showInJei(ItemStack stack) {
        IJeiRuntime runtime = jeiRuntime;
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


