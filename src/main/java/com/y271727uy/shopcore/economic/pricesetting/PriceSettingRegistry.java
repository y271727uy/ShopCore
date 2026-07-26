package com.y271727uy.shopcore.economic.pricesetting;

import com.y271727uy.shopcore.client.TooltipTitleRegistry;
import com.y271727uy.shopcore.economic.price.PriceRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class PriceSettingRegistry {
    private static final List<PriceSettingDefinition> DEFINITIONS = new ArrayList<>();

    private PriceSettingRegistry() {
    }

    public static void clear() {
        DEFINITIONS.clear();
        PriceRegistry.clear();
        TooltipTitleRegistry.clear();
        PriceSettingMenus.clear();
    }

    public static PriceSettingDefinition register(PriceSettingDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        DEFINITIONS.add(definition);
        apply(definition);
        return definition;
    }

    public static PriceSettingDefinition register(PriceSettingDefinition.Builder builder) {
        return register(Objects.requireNonNull(builder, "builder").build());
    }

    public static List<PriceSettingDefinition> definitions() {
        return List.copyOf(DEFINITIONS);
    }

    public static void rebuildRuntimeRegistries() {
        PriceRegistry.clear();
        TooltipTitleRegistry.clear();
        PriceSettingMenus.clear();
        for (PriceSettingDefinition definition : DEFINITIONS) {
            apply(definition);
        }
    }

    private static void apply(PriceSettingDefinition definition) {
        PriceRegistry.registerEntry(
                definition.itemIdOrTagId(),
                definition.price().basicPrice(),
                definition.price().addPrice(),
                definition.price().reputation()
        );
        definition.tooltipTitleKey().ifPresent(translationKey -> {
            if (definition.tagReference()) {
                TooltipTitleRegistry.registerTagTitle(definition.itemIdOrTagId(), translationKey);
            } else {
                TooltipTitleRegistry.registerItemTitle(definition.itemIdOrTagId(), translationKey);
            }
        });
        for (String menuId : definition.menuIds()) {
            PriceSettingMenus.add(menuId, definition.itemIdOrTagId());
        }
    }
}
