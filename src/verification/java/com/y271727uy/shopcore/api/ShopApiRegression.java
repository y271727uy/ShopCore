package com.y271727uy.shopcore.api;

import com.y271727uy.shopcore.api.component.consumer.ConsumerOwnershipTarget;
import com.y271727uy.shopcore.api.component.menu.MenuListing;
import com.y271727uy.shopcore.api.component.menu.MenuSource;
import com.y271727uy.shopcore.api.component.menu.ShopcoreMenus;
import com.y271727uy.shopcore.api.component.economic.*;
import com.y271727uy.shopcore.api.preset.ShopPresets;
import com.y271727uy.shopcore.api.shop.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Exercises the assembled runtime through public contracts, without a world or optional NPC spawning. */
public final class ShopApiRegression {
    private static int checks;

    public static void main(String[] args) {
        publicBoundary();
        removedContracts();
        economicComponents();
        menuDefinitions();
        referenceConsumersUseApi();
        perShopStateAndPersistence();
        System.out.println("Shop API regression passed: " + checks + " checks");
    }

    private static void publicBoundary() {
        // Discover every compiled API class so future additions cannot silently escape this check.
        try {
            // A package resource lookup can select this test's api/ directory first on the classpath.
            Path directory = Path.of(ShopcoreApi.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                    .resolve("com/y271727uy/shopcore/api");
            boolean foundEntrypoint = false;
            try (var files = Files.walk(directory)) {
                for (Path file : files.filter(path -> path.toString().endsWith(".class")).toList()) {
                    String relative = directory.relativize(file).toString().replace('\\', '.').replace('/', '.');
                    Class<?> type = Class.forName("com.y271727uy.shopcore.api." + relative.substring(0, relative.length() - 6),
                            false, ShopApiRegression.class.getClassLoader());
                    if (!Modifier.isPublic(type.getModifiers())) continue;
                    if (type == ShopcoreApi.class) foundEntrypoint = true;
                    if (type.getGenericSuperclass() != null) publicType(type.getGenericSuperclass());
                    for (Type parent : type.getGenericInterfaces()) publicType(parent);
                    for (var parameter : type.getTypeParameters()) {
                        for (Type bound : parameter.getBounds()) publicType(bound);
                    }
                    for (var field : type.getDeclaredFields()) {
                        if (visible(field.getModifiers())) publicType(field.getGenericType());
                    }
                    for (var method : type.getDeclaredMethods()) {
                        if (!visible(method.getModifiers())) continue;
                        publicType(method.getGenericReturnType());
                        for (Type parameter : method.getGenericParameterTypes()) publicType(parameter);
                        for (Type exception : method.getGenericExceptionTypes()) publicType(exception);
                        for (var parameter : method.getTypeParameters()) {
                            for (Type bound : parameter.getBounds()) publicType(bound);
                        }
                    }
                    for (var constructor : type.getDeclaredConstructors()) {
                        if (!visible(constructor.getModifiers())) continue;
                        for (Type parameter : constructor.getGenericParameterTypes()) publicType(parameter);
                    }
                }
            }
            check(foundEntrypoint, "boundary scan must cover the production API, not the regression package");
        } catch (IOException | URISyntaxException | ReflectiveOperationException ex) {
            throw new AssertionError("cannot inspect the complete API", ex);
        }
    }

    private static boolean visible(int modifiers) {
        return Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers);
    }

    private static void publicType(Type type) {
        check(!java.util.regex.Pattern.compile("com\\.y271727uy\\.shopcore\\.(?!api\\.)")
                .matcher(type.getTypeName()).find(), "public signature leaks implementation: " + type);
    }

    private static void removedContracts() {
        for (String name : List.of("api/economic/ShopcorePrices", "api/economic/ShopcoreCurrency",
                "api/economic/ShopcoreCheckout", "api/economic/ShopcoreReputation", "api/economic/PriceProvider",
                "api/economic/PriceQuote", "api/shop_menu/ShopcoreMenus", "api/shop_menu/MenuProvider",
                "core/shop/blockentity/AbstractShopBlockEntity", "core/shop/shopmenu/TooltipMenuCreate",
                "core/consumer/common/interaction/ConsumerInteractionTarget",
                "core/consumer/common/lifecycle/ConsumerOwnershipTarget", "text/TestShopCustomerInteraction")) {
            check(ShopApiRegression.class.getResource("/com/y271727uy/shopcore/" + name + ".class") == null,
                    "removed compatibility contract was retained: " + name);
        }
    }

    private static void economicComponents() {
        var quote = new PriceQuote(10, 3, 2);
        var input = new CheckoutInput(quote.baseValue(), quote.additionalValue(), quote.reputationValue(), 3, 1.5D);
        var result = ShopcoreCheckout.calculate(input);
        check(result.totalValue() == 59L && result.reputation() == 9D, "rounding and reputation use the same order input");
        check(result.currency().isEmpty(), "calculation does not create items or deposit money");
        for (long amount : new long[]{0, 1, 2, 7, 8, 127, 128, 1024, 2000, 131072, 131073, 400000}) {
            var payout = ShopcoreCheckout.calculate(new CheckoutInput(amount, 0, 0, 1, 1));
            check(payout.payouts().stream().mapToLong(CurrencyPayout::totalValue).sum() == amount,
                    "denomination split preserves value: " + amount);
        }
        List<CurrencyPayout> supplied = new ArrayList<>(result.payouts());
        var copy = new CheckoutResult(List.of(), supplied, result.totalValue(), result.reputation());
        supplied.clear();
        check(!copy.payouts().isEmpty(), "result owns its payout list");
        expectUnsupported(() -> copy.payouts().clear());
        expectUnsupported(() -> CurrencyDenomination.descendingValues().set(0, CurrencyDenomination.COPPER_GT_CREDIT));
        long[] factoryValue = {0};
        ShopcoreCheckout.checkout(input, (denomination, count) -> {
            factoryValue[0] += denomination.totalValue(count);
            return null; // This headless factory records requests without requiring item registries.
        });
        check(factoryValue[0] == result.totalValue(), "public factory receives the calculated payout");
        try {
            ShopcoreCheckout.checkout(input, null);
            throw new AssertionError("null factory should use the explicit calculate entry point");
        } catch (NullPointerException expected) { checks++; }
        check(CurrencyDenomination.fromItemId(ResourceLocation.fromNamespaceAndPath("list", "copper_gt_credit"))
                .orElseThrow() == CurrencyDenomination.COPPER_GT_CREDIT, "currency id mapping is retained");
    }

    private static void menuDefinitions() {
        String name = "shopcore:api_regression";
        check(ShopcoreMenus.findTag(name).isEmpty(), "unknown menu alias is absent");
        ShopcoreMenus.registerTag("  " + name + "  ", "#minecraft:logs");
        check(ShopcoreMenus.findTag(name).orElseThrow().toString().equals("minecraft:logs"),
                "registration and lookup normalize the same id");
        ShopcoreMenus.registerTag(name, "minecraft:planks");
        check(ShopcoreMenus.findTag(name).orElseThrow().toString().equals("minecraft:planks"),
                "re-registering replaces the alias definition");
        try {
            ShopcoreMenus.registerTag(name, "bad tag!");
            throw new AssertionError("invalid tag must fail registration");
        } catch (IllegalArgumentException expected) { checks++; }
        check(ShopcoreMenus.findTag(name).orElseThrow().toString().equals("minecraft:planks"),
                "failed registration leaves the old definition intact");
    }

    private static void expectUnsupported(Runnable action) {
        try {
            action.run();
            throw new AssertionError("public shared values must be immutable");
        } catch (UnsupportedOperationException expected) { checks++; }
    }

    private static void referenceConsumersUseApi() {
        for (String resource : List.of("text/TestShopBlockEntity.class", "item/card/BankCardItem.class",
                "integration/jade/ShopReputationTooltipProvider.class", "client/PriceTooltipHandler.class")) {
            try (var stream = ShopApiRegression.class.getResourceAsStream("/com/y271727uy/shopcore/" + resource)) {
                check(stream != null, "reference consumer class is available: " + resource);
                String bytecode = new String(stream.readAllBytes(), StandardCharsets.ISO_8859_1);
                check(!bytecode.contains("com/y271727uy/shopcore/core/"), "reference consumer bypasses the API: " + resource);
            } catch (IOException ex) {
                throw new AssertionError("cannot inspect API consumer " + resource, ex);
            }
        }
    }

    private static void perShopStateAndPersistence() {
        MenuListing[] supplied = {new MenuListing("minecraft:apple", 4, 4)};
        MenuSource menu = MenuSource.fixed(supplied);
        supplied[0] = new MenuListing("minecraft:potato", 2, 4);
        var definition = ShopPresets.horizontalQueue(ResourceLocation.fromNamespaceAndPath("shopcore", "api_regression"),
                menu, "TestConsumers", "test.shopcore.delivery");
        Host first = new Host(definition);
        Host second = new Host(definition);
        check(ShopcoreShops.isShop(first), "public host is discoverable without the old internal base class");
        check(ShopcoreShops.snapshot(first).isEmpty(), "read-only query does not create an instance");
        check(!first.shopRuntime().status().open(), "new shop starts closed");
        check(first.shopRuntime().toggleOpenRequested(), "opening request is accepted before level attachment");
        check(first.shopRuntime().status().openRequested(), "runtime owns the opening request");
        check(!second.shopRuntime().status().openRequested(), "shared definition does not share opening state");
        UUID account = new UUID(0, 321);
        UUID other = new UUID(0, 322);
        check(ShopcoreShops.bindSettlementAccount(first, account, true) == ShopAccountBindingResult.BOUND, "bind through API");
        check(first.dirty > 0, "account and opening changes request persistence");
        check(ShopcoreShops.bindSettlementAccount(first, account, false) == ShopAccountBindingResult.ALREADY_BOUND_TO_ACCOUNT,
                "repeat bind is idempotent");
        check(ShopcoreShops.bindSettlementAccount(first, other, false) == ShopAccountBindingResult.BOUND_TO_OTHER_ACCOUNT,
                "another account cannot replace the owner");
        check(ShopcoreShops.snapshot(second).orElseThrow().settlementAccount().isEmpty(), "per-host accounts");
        check(!ShopcoreShops.snapshot(first).orElseThrow().id().equals(ShopcoreShops.snapshot(second).orElseThrow().id()),
                "per-host shop identity");

        CompoundTag saved = new CompoundTag();
        first.shopRuntime().save(saved);
        check(saved.contains("ShopRuntime", Tag.TAG_COMPOUND), "existing shop storage root is retained");
        check(saved.contains("TestConsumers", Tag.TAG_LIST), "existing consumer storage root is retained");
        var entries = saved.getCompound("ShopRuntime").getCompound("Menu").getList("Entries", Tag.TAG_COMPOUND);
        check(entries.size() == 1 && entries.getCompound(0).getString("MenuId").equals("minecraft:apple"),
                "public menu initializes real persisted offers and defensively copies input");

        Host restored = new Host(definition);
        restored.shopRuntime().load(saved);
        check(ShopcoreShops.snapshot(first).equals(ShopcoreShops.snapshot(restored)), "saved identity and account round trip");
        check(restored.shopRuntime().status().openRequested(), "saved opening request round trip");
        check(restored.dirty == 0, "loading alone does not request another save");
        Host client = new Host(definition);
        client.shopRuntime().handleUpdateTag(first.shopRuntime().updateTag());
        check(ShopcoreShops.snapshot(first).equals(ShopcoreShops.snapshot(client)), "client sync uses the same shop snapshot");
        check(!first.shopRuntime().updateTag().contains("TestConsumers"), "consumer roster is not added to runtime sync");

        CompoundTag legacy = saved.copy();
        legacy.remove("TestConsumersOrderBindings");
        UUID consumer = new UUID(0, 700);
        ListTag roster = new ListTag();
        roster.add(NbtUtils.createUUID(consumer));
        legacy.put("TestConsumers", roster);
        Host legacyHost = new Host(definition);
        legacyHost.shopRuntime().load(legacy);
        check(legacyHost.ownsConsumer(consumer), "old consumer list is loaded through the public host");
        check(!ConsumerOwnershipTarget.shouldDetach(true, legacyHost, consumer), "recovery recognizes the API host's roster");
        legacyHost.consumerRemoved(consumer);
        check(!legacyHost.ownsConsumer(consumer) && legacyHost.dirty == 1, "removal notification releases and saves the roster");

        check(ShopcoreShops.unbindSettlementAccount(first, other) == ShopAccountBindingResult.BOUND_TO_OTHER_ACCOUNT,
                "wrong account cannot unbind");
        check(ShopcoreShops.unbindSettlementAccount(first, account) == ShopAccountBindingResult.UNBOUND, "owner can unbind");
        check(ShopcoreShops.unbindSettlementAccount(first, account) == ShopAccountBindingResult.NO_ACCOUNT_BOUND,
                "already unbound is distinct from uninitialized");
        check(ShopcoreShops.snapshot(restored).orElseThrow().hasSettlementAccount(), "saved instances are independent");
        check(ConsumerOwnershipTarget.shouldDetach(true, first, UUID.randomUUID()), "public host participates in orphan recovery");
        check(!ConsumerOwnershipTarget.shouldDetach(false, first, UUID.randomUUID()), "unloaded owner is not treated as removed");
    }

    private static final class Host extends ShopBlockEntity {
        private int dirty;

        private Host(ShopDefinition definition) {
            // No world, registry-backed block type or block state is used by these persistence checks.
            super(null, BlockPos.ZERO, null, definition);
        }

        @Override
        public void setChanged() { dirty++; }
    }

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
}
