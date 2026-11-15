package com.y271727uy.shopcore.gameplay.sellingbin;

import com.mojang.logging.LogUtils;
import com.y271727uy.shopcore.ShopcoreMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 虚拟库存倍率。文件位于 config/shopcore/trade_balance_multiplier.txt。
 * 每一行只能是 {@code namespace:path = 正整数}，未列出的物品按 1 倍计入。
 */
public final class TradeBalanceMultiplierConfig {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String FILE_NAME = "trade_balance_multiplier.txt";
    private static final int MAX_MULTIPLIER = 1_000_000;

    private static volatile Map<ResourceLocation, Integer> multipliers = Map.of();

    private TradeBalanceMultiplierConfig() {
    }

    public static void reset() {
        multipliers = Map.of();
    }

    public static void load() {
        Path file = configFile();
        try {
            Files.createDirectories(file.getParent());
            if (Files.notExists(file)) {
                Files.writeString(file, defaultText(), StandardCharsets.UTF_8);
                multipliers = Map.of();
                LOGGER.info("Created ShopCore trade balance multiplier config at {}", file);
                return;
            }

            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            ParseResult parsed = parse(lines);
            if (!parsed.errors.isEmpty()) {
                LOGGER.error(
                        "ShopCore trade balance multiplier config was rejected. Keeping the previous {} entries. Fix {}:",
                        multipliers.size(),
                        file
                );
                for (String error : parsed.errors) {
                    LOGGER.error("{}", error);
                }
                return;
            }

            multipliers = Collections.unmodifiableMap(parsed.multipliers);
            LOGGER.info("Loaded {} ShopCore trade balance multipliers", multipliers.size());
        } catch (IOException exception) {
            LOGGER.error("Failed to read ShopCore trade balance multiplier config at {}", file, exception);
        }
    }

    public static int multiplierFor(ResourceLocation itemId) {
        if (itemId == null) {
            return 1;
        }
        return multipliers.getOrDefault(itemId, 1);
    }

    public static int scaledVirtualStock(ResourceLocation itemId, int soldCount) {
        if (itemId == null || soldCount <= 0) {
            return 0;
        }

        long scaled = (long) soldCount * multiplierFor(itemId);
        if (scaled > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) scaled;
    }

    static ParseResult parse(List<String> lines) {
        Map<ResourceLocation, Integer> parsedMultipliers = new LinkedHashMap<>();
        List<String> errors = new ArrayList<>();

        for (int index = 0; index < lines.size(); index++) {
            String rawLine = lines.get(index);
            String line = stripComment(rawLine).trim();
            if (line.isEmpty()) {
                continue;
            }

            int lineNumber = index + 1;
            int separator = line.indexOf('=');
            if (separator <= 0 || line.indexOf('=', separator + 1) != -1) {
                errors.add(formatError(lineNumber, "必须且只能有一个 '='，格式为 namespace:path = 正整数"));
                continue;
            }

            String itemText = line.substring(0, separator).trim();
            String multiplierText = line.substring(separator + 1).trim();
            ResourceLocation itemId = ResourceLocation.tryParse(itemText);
            if (!isCanonicalItemId(itemText, itemId)) {
                errors.add(formatError(lineNumber, "物品 id 必须是小写的 namespace:path"));
                continue;
            }
            if (!isPositiveInteger(multiplierText)) {
                errors.add(formatError(lineNumber, "倍率必须是不带符号、小数或单位的正整数"));
                continue;
            }

            int multiplier;
            try {
                multiplier = Integer.parseInt(multiplierText);
            } catch (NumberFormatException exception) {
                errors.add(formatError(lineNumber, "倍率超出允许范围 1-" + MAX_MULTIPLIER));
                continue;
            }
            if (multiplier > MAX_MULTIPLIER) {
                errors.add(formatError(lineNumber, "倍率超出允许范围 1-" + MAX_MULTIPLIER));
                continue;
            }
            if (parsedMultipliers.containsKey(itemId)) {
                errors.add(formatError(lineNumber, "物品 id 重复: " + itemId));
                continue;
            }

            parsedMultipliers.put(itemId, multiplier);
        }

        return new ParseResult(parsedMultipliers, errors);
    }

    private static boolean isCanonicalItemId(String text, ResourceLocation itemId) {
        return itemId != null && itemId.toString().equals(text.toLowerCase(Locale.ROOT));
    }

    private static boolean isPositiveInteger(String text) {
        if (text.isEmpty() || text.charAt(0) == '0') {
            return false;
        }
        for (int index = 0; index < text.length(); index++) {
            if (!Character.isDigit(text.charAt(index))) {
                return false;
            }
        }
        return true;
    }

    private static String stripComment(String line) {
        int comment = line.indexOf('#');
        return comment < 0 ? line : line.substring(0, comment);
    }

    private static String formatError(int lineNumber, String message) {
        return "第 " + lineNumber + " 行: " + message;
    }

    private static Path configFile() {
        return FMLPaths.CONFIGDIR.get().resolve(ShopcoreMod.MODID).resolve(FILE_NAME);
    }

    private static String defaultText() {
        return """
                # ShopCore 虚拟库存倍率
                # 路径: config/shopcore/trade_balance_multiplier.txt
                #
                # 每一行只能写成:
                # namespace:path = 正整数
                #
                # 正整数表示出售数量乘以多少后计入虚拟库存。
                # 1 表示出售多少就增加多少；10 表示增加十倍。
                # 没有写在这里的物品默认是 1。
                # 物品 id 必须小写，倍率不能是 0、负数、小数或带单位的数字。
                # 任意一行格式错误时，整份配置会被拒绝，并继续使用上一份有效配置。
                #
                # 示例（默认不启用，删除行首 # 后生效）:
                # minecraft:apple = 1
                # minecraft:diamond = 10
                #
                # ShopCore virtual-stock multiplier
                # Path: config/shopcore/trade_balance_multiplier.txt
                #
                # Each entry must use exactly this form:
                # namespace:path = positive integer
                #
                # The integer multiplies the sold amount before it is added to virtual stock.
                # 1 adds the sold amount as-is; 10 adds ten times that amount.
                # Items not listed here use a multiplier of 1.
                # Item IDs must be lowercase. The multiplier cannot be 0, negative, decimal, or include a unit.
                # If any line has an invalid format, the whole file is rejected and the last valid config stays active.
                #
                # Examples (disabled by default; remove the leading # to enable):
                # minecraft:apple = 1
                # minecraft:diamond = 10
                """;
    }

    record ParseResult(Map<ResourceLocation, Integer> multipliers, List<String> errors) {
    }
}
