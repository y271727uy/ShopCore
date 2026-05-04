package com.y271727uy.shopcore.gameplay.sellingbin;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@SuppressWarnings("unused")
public final class SellingBinSeasonalPriceRules {
    private static final List<SeasonalPriceRule> RULES = new ArrayList<>();
    private static final Comparator<SeasonalPriceRule> RULE_ORDER = Comparator
            .comparing(SeasonalPriceRule::seasonId)
            .thenComparing(SeasonalPriceRule::groupName)
            .thenComparingInt(SeasonalPriceRule::count)
            .thenComparingInt(SeasonalPriceRule::minBonus)
            .thenComparingInt(SeasonalPriceRule::maxBonus);
    private static boolean defaultsRegistered;

    private SellingBinSeasonalPriceRules() {
    }

    public static void clear() {
        RULES.clear();
        defaultsRegistered = false;
    }

    public static SeasonalPriceRule register(SeasonalPriceRule rule) {
        Objects.requireNonNull(rule, "rule");
        RULES.add(rule);
        RULES.sort(RULE_ORDER);
        return rule;
    }

    public static RuleBuilder rule(String seasonId, String groupName) {
        return new RuleBuilder(seasonId, groupName);
    }

    public static RuleBuilder spring(String groupName) {
        return rule("spring", groupName);
    }

    /**
     * 默认季节规则统一从这里注册。
     *
     * <p>新增规则推荐直接在这里继续加：
     * <pre>
     * registerDefaults() {
     *     winterCrop().count(Integer.MAX_VALUE).bonus(1).register();
     *     spring("logs").count(3).bonus(2).register();
     *     summer("fish").count(2).bonus(1, 3).register();
     * }
     * </pre>
     */
    public static void registerDefaults() {
        if (defaultsRegistered) {
            return;
        }

        defaultsRegistered = true;
        winterCrop().count(Integer.MAX_VALUE).bonus(1).register();
    }

    /**
     * 便捷写法：冬季 + crop 组。
     *
     * <p>常用注册方式：
     * <pre>
     * winterCrop().count(Integer.MAX_VALUE).bonus(1).register();
     * // 或者
     * winterCrop(3, 1, 2);
     * </pre>
     */
    public static RuleBuilder winterCrop() {
        return winter("crop");
    }

    /**
     * 直接注册冬季 crop 组规则。
     *
     * <p>参数说明：
     * <ul>
     *   <li><code>count</code>：最多影响多少个条目，超过组数量会自动按实际数量处理</li>
     *   <li><code>minBonus</code> / <code>maxBonus</code>：涨价幅度范围</li>
     * </ul>
     */
    public static SeasonalPriceRule winterCrop(int count, int minBonus, int maxBonus) {
        return winterCrop().count(count).bonus(minBonus, maxBonus).register();
    }

    public static RuleBuilder summer(String groupName) {
        return rule("summer", groupName);
    }

    public static RuleBuilder autumn(String groupName) {
        return rule("autumn", groupName);
    }

    public static RuleBuilder winter(String groupName) {
        return rule("winter", groupName);
    }

    public static List<SeasonalPriceRule> rules() {
        return List.copyOf(RULES);
    }

    private static String normalizeSeasonId(String seasonId) {
        String normalized = Objects.requireNonNull(seasonId, "seasonId").trim().toLowerCase(Locale.ROOT);
        if ("fall".equals(normalized)) {
            return "autumn";
        }
        return normalized;
    }

    private static String normalizeGroupName(String groupName) {
        return Objects.requireNonNull(groupName, "groupName").trim();
    }

    public static final class RuleBuilder {
        private final String seasonId;
        private final String groupName;
        private int count = 1;
        private int minBonus = 1;
        private int maxBonus = 1;

        private RuleBuilder(String seasonId, String groupName) {
            this.seasonId = normalizeSeasonId(seasonId);
            this.groupName = normalizeGroupName(groupName);
        }

        public RuleBuilder count(int count) {
            this.count = Math.max(0, count);
            return this;
        }

        public RuleBuilder bonus(int bonus) {
            return bonus(bonus, bonus);
        }

        public RuleBuilder bonus(int minBonus, int maxBonus) {
            this.minBonus = Math.max(0, minBonus);
            this.maxBonus = Math.max(this.minBonus, Math.max(0, maxBonus));
            return this;
        }

        public SeasonalPriceRule register() {
            return SellingBinSeasonalPriceRules.register(new SeasonalPriceRule(seasonId, groupName, count, minBonus, maxBonus));
        }
    }

    public record SeasonalPriceRule(String seasonId, String groupName, int count, int minBonus, int maxBonus) {
        public SeasonalPriceRule {
            seasonId = normalizeSeasonId(seasonId);
            groupName = normalizeGroupName(groupName);
            count = Math.max(0, count);
            minBonus = Math.max(0, minBonus);
            maxBonus = Math.max(minBonus, Math.max(0, maxBonus));
        }

        public boolean matchesSeason(String currentSeasonId) {
            if (currentSeasonId == null || currentSeasonId.isBlank()) {
                return false;
            }

            return seasonId.equalsIgnoreCase(normalizeSeasonId(currentSeasonId));
        }

        public int bonusFor(ResourceLocation priceKey) {
            if (maxBonus <= minBonus) {
                return minBonus;
            }

            int range = maxBonus - minBonus + 1;
            int seed = Objects.hash(seasonId, groupName, count, minBonus, maxBonus, priceKey.toString());
            return minBonus + Math.floorMod(seed, range);
        }
    }
}
