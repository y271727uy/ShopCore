package com.y271727uy.shopcore.economic.pricesetting;

/**
 * Small facade for hand-maintained price setting DSL files.
 */
public final class PriceSettings {
    private PriceSettings() {
    }

    public static PriceSettingDefinition.Builder item(String itemId) {
        return PriceSettingDefinition.item(itemId);
    }

    public static PriceSettingDefinition.Builder tag(String tagId) {
        return PriceSettingDefinition.tag(tagId);
    }

    public static PriceSettingDefinition register(PriceSettingDefinition.Builder builder) {
        return PriceSettingRegistry.register(builder);
    }

    public static PriceSettingDefinition register(PriceSettingDefinition definition) {
        return PriceSettingRegistry.register(definition);
    }
}
