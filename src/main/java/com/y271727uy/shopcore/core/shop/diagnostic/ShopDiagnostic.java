package com.y271727uy.shopcore.core.shop.diagnostic;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;

public record ShopDiagnostic(
        ShopDiagnosticCode code,
        List<Component> details
) {
    public ShopDiagnostic {
        Objects.requireNonNull(code, "code");
        details = List.copyOf(Objects.requireNonNull(details, "details"));
    }

    public static ShopDiagnostic of(ShopDiagnosticCode code) {
        return new ShopDiagnostic(code, List.of());
    }

    public static ShopDiagnostic of(ShopDiagnosticCode code, List<Component> details) {
        return new ShopDiagnostic(code, details);
    }

    public ResourceLocation id() {
        return code.id();
    }

    public String translationKey() {
        return code.translationKey();
    }

    public Component message() {
        return Component.translatable(translationKey());
    }
}
