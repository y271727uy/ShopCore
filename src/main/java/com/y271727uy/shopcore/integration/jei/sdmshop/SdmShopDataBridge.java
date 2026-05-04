package com.y271727uy.shopcore.integration.jei.sdmshop;

import com.mojang.logging.LogUtils;
import com.y271727uy.shopcore.economic.CurrencyDenomination;
import com.y271727uy.shopcore.economic.Price;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Collection;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.concurrent.ConcurrentHashMap;

public final class SdmShopDataBridge {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String DEFAULT_CURRENCY = ResourceLocation.fromNamespaceAndPath("list", CurrencyDenomination.COPPER_GT_CREDIT.itemPath()).toString();

    private static final Set<SdmShopJeiEntry> ENTRY_CACHE = ConcurrentHashMap.newKeySet();
    private static volatile List<SdmShopJeiEntry> cachedEntries = List.of();
    private static final Object CACHE_LOCK = new Object();

    private static final List<String> PROVIDER_CLASSES = List.of(
            "net.sixik.sdmshoprework.common.integration.KubeJS.ShopJSPlugin",
            "net.sixik.sdmshoprework.common.shop.ShopAPI",
            "net.sixik.sdmshoprework.common.shop.ShopManager",
            "net.sixik.sdmshoprework.common.shop.ShopDataManager",
            "net.sixik.sdmshoprework.common.shop.ShopRegistry",
            "net.sixik.sdmshoprework.common.shop.ShopUtils"
    );

    private static final List<String> SCAN_PREFIXES = List.of(
            "net.sixik.sdmshoprework",
            "net.sixik.sdmshoprework.common",
            "net.sixik.sdmshoprework.client",
            "net.sixik.sdmshoprework.server"
    );

    private static final List<String> ENTRY_METHODS = List.of(
            "getAllShopItems",
            "getShopItems",
            "getEntries",
            "getTabs",
            "getShopTabs",
            "getAllTabs",
            "getShopEntries",
            "getShopList"
    );

    private static final List<String> STACK_METHODS = List.of(
            "getItemStack",
            "getStack",
            "stack",
            "getItem",
            "getOutput",
            "output",
            "getResult",
            "result",
            "getSellItem",
            "getDisplayItem",
            "itemStack",
            "item",
            "sellItem",
            "displayItem"
    );

    private static final List<String> PRICE_METHODS = List.of(
            "getPrice",
            "getCurrentPrice",
            "price",
            "getBuyPrice",
            "getSellPrice",
            "getCost",
            "cost",
            "entryPrice",
            "entryCost",
            "basicPrice",
            "addPrice"
    );

    private static final List<String> NAME_METHODS = List.of(
            "getShopName",
            "getName",
            "shopName",
            "name",
            "getTitle",
            "title",
            "getLangKey",
            "getCurrentLanguageKey",
            "getTranslationKey"
    );

    private static final List<String> QUANTITY_METHODS = List.of(
            "getQuantity",
            "quantity",
            "getCount",
            "count",
            "getAmount",
            "amount",
            "entryCount"
    );

    private static final List<String> LOCKED_METHODS = List.of(
            "isLocked",
            "locked",
            "hasLock",
            "getLocked",
            "isLockedByQuest",
            "isLock"
    );

    private static final List<String> LOCK_REASON_METHODS = List.of(
            "getLockedReason",
            "lockedReason",
            "getLockReason",
            "getReason",
            "getQuestTitle"
    );

    private static final List<String> ENTRY_CLASS_NAMES = List.of(
            "net.sixik.sdmshoprework.common.shop.ShopEntry",
            "net.sixik.sdmshoprework.common.shop.entry.ShopEntry",
            "net.sixik.sdmshoprework.api.shop.ShopEntry"
    );

    private static final List<String> QUEST_ID_METHODS = List.of(
            "getQuestId",
            "questId",
            "getPreTask",
            "preTask",
            "getRequiredQuest",
            "requiredQuest"
    );

    private static final List<String> FIELD_NAMES = List.of(
            "shopTabs",
            "tabs",
            "entries",
            "items",
            "shopItems",
            "allShopItems",
            "allTabs",
            "shops",
            "list"
    );

    private static final Comparator<SdmShopJeiEntry> ENTRY_ORDER = Comparator
            .comparing(SdmShopJeiEntry::shopName, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(SdmShopJeiEntry::displayNameKey, String.CASE_INSENSITIVE_ORDER)
            .thenComparingInt(entry -> entry.price().totalPrice());

    private SdmShopDataBridge() {
    }

    public static List<SdmShopJeiEntry> collectEntries() {
        if (!ENTRY_CACHE.isEmpty()) {
            rebuildCache();
            return cachedEntries;
        }

        List<SdmShopJeiEntry> configEntries = loadEntriesFromConfig();
        if (!configEntries.isEmpty()) {
            synchronized (CACHE_LOCK) {
                cachedEntries = List.copyOf(configEntries);
                ENTRY_CACHE.addAll(configEntries);
                return cachedEntries;
            }
        }

        List<Object> rawEntries = collectRawEntries();
        List<SdmShopJeiEntry> entries = new ArrayList<>();

        for (Object raw : rawEntries) {
            SdmShopJeiEntry entry = toEntry(raw);
            if (entry != null) {
                entries.add(entry);
            }
        }

        entries.sort(ENTRY_ORDER);
        synchronized (CACHE_LOCK) {
            cachedEntries = List.copyOf(entries);
            return cachedEntries;
        }
    }

    public static void recordEntry(SdmShopJeiEntry entry) {
        if (entry == null) {
            return;
        }
        if (ENTRY_CACHE.add(entry)) {
            rebuildCache();
        }
    }

    public static List<SdmShopJeiEntry> snapshot() {
        if (cachedEntries.isEmpty() && !ENTRY_CACHE.isEmpty()) {
            rebuildCache();
        }
        return cachedEntries;
    }

    public static List<SdmShopJeiEntry> refreshEntries() {
        if (!ENTRY_CACHE.isEmpty()) {
            rebuildCache();
            return cachedEntries;
        }
        return collectEntries();
    }

    public static SdmShopJeiEntry findByStack(ItemStack stack) {
        Objects.requireNonNull(stack, "stack");
        return collectEntries().stream()
                .filter(entry -> entry.itemStack().is(stack.getItem()))
                .findFirst()
                .orElse(null);
    }

    public static String getPriceText(SdmShopJeiEntry entry) {
        Objects.requireNonNull(entry, "entry");
        return entry.priceText();
    }

    private static List<SdmShopJeiEntry> loadEntriesFromConfig() {
        for (Path path : List.of(
                FMLPaths.CONFIGDIR.get().resolve("SDMShop/sdmshop.snbt"),
                FMLPaths.CONFIGDIR.get().resolve("sdmshop.snbt")
        )) {
            if (!Files.isRegularFile(path)) {
                continue;
            }

            try {
                String snbt = Files.readString(path);
                List<SdmShopJeiEntry> parsed = parseSdmShopConfig(TagParser.parseTag(snbt));
                if (!parsed.isEmpty()) {
                    LOGGER.info("Loaded {} SDM shop entries from {}", parsed.size(), path);
                    return parsed;
                }
            } catch (Exception exception) {
                LOGGER.warn("Failed to read SDM shop config {}", path, exception);
            }
        }

        return List.of();
    }

    private static List<SdmShopJeiEntry> parseSdmShopConfig(CompoundTag root) {
        List<SdmShopJeiEntry> entries = new ArrayList<>();
        if (root == null || !root.contains("shopTabs", Tag.TAG_LIST)) {
            return entries;
        }

        ListTag shopTabs = root.getList("shopTabs", Tag.TAG_COMPOUND);
        for (Tag tabElement : shopTabs) {
            if (!(tabElement instanceof CompoundTag tabTag)) {
                continue;
            }

            String shopName = tabTag.getString("title");
            if (shopName.isBlank()) {
                shopName = "SDM Shop";
            }

            ListTag tabEntries = tabTag.getList("tabEntry", Tag.TAG_COMPOUND);
            for (Tag entryElement : tabEntries) {
                if (!(entryElement instanceof CompoundTag entryTag)) {
                    continue;
                }

                CompoundTag entryType = entryTag.getCompound("entryType");
                if (!entryType.contains("shopEntryTypeID", Tag.TAG_STRING)
                        || !"shopItemEntryType".equals(entryType.getString("shopEntryTypeID"))) {
                    continue;
                }

                CompoundTag itemStackTag = entryType.getCompound("itemStack");
                ItemStack stack = ItemStack.of(itemStackTag);
                if (stack.isEmpty()) {
                    continue;
                }

                long priceValue = entryTag.contains("entryPrice", Tag.TAG_LONG) ? entryTag.getLong("entryPrice") : 0L;
                int quantity = Math.max(1, entryTag.contains("entryCount", Tag.TAG_INT) ? entryTag.getInt("entryCount") : stack.getCount());
                Price price = Price.of((int) Math.max(0L, priceValue), 0, 0);
                boolean sell = entryTag.contains("isSell", Tag.TAG_BYTE) && entryTag.getBoolean("isSell");
                boolean locked = entryTag.contains("isLocked", Tag.TAG_BYTE) && entryTag.getBoolean("isLocked");
                String lockReason = "";

                entries.add(new SdmShopJeiEntry(stack, price, quantity, shopName, DEFAULT_CURRENCY, sell, locked, lockReason));
            }
        }

        return entries;
    }

    public static Optional<Object> invokeFirst(Object target, String... names) {
        if (target == null || names == null || names.length == 0) {
            return Optional.empty();
        }
        return invokeAny(target, List.of(names));
    }

    public static Optional<Object> invokeFirst(Object target, Object argument, String... names) {
        if (target == null || argument == null || names == null) {
            return Optional.empty();
        }
        for (String name : names) {
            Optional<Object> value = invokeSingleArg(target, name, argument);
            if (value.isPresent()) {
                return value;
            }
        }
        return Optional.empty();
    }

    public static Optional<ItemStack> extractItemStackValue(Object raw) {
        return extractItemStack(raw);
    }

    public static Optional<Price> extractPriceValue(Object raw) {
        return extractPrice(raw);
    }

    public static Optional<String> extractStringValue(Object raw, String... names) {
        return extractString(raw, List.of(names));
    }

    public static Optional<Integer> extractIntValue(Object raw, String... names) {
        return extractInt(raw, List.of(names));
    }

    public static Optional<Boolean> extractBooleanValue(Object raw, String... names) {
        return extractBoolean(raw, List.of(names));
    }

    public static Optional<Object> createShopEntrySnapshot(Object shopTab, CompoundTag nbt) {
        if (shopTab == null || nbt == null) {
            return Optional.empty();
        }

        for (String className : ENTRY_CLASS_NAMES) {
            Class<?> entryClass = tryLoadClass(className);
            if (entryClass == null) {
                continue;
            }

            Optional<Object> entry = tryInstantiateEntry(entryClass, shopTab);
            if (entry.isEmpty()) {
                continue;
            }

            Object value = entry.get();
            if (invokeSingleArg(value, "deserializeNBT", nbt).isPresent()
                    || invokeSingleArg(value, "deserialize", nbt).isPresent()
                    || invokeSingleArg(value, "load", nbt).isPresent()
                    || invokeSingleArg(value, "read", nbt).isPresent()) {
                return entry;
            }
        }

        return Optional.empty();
    }

    private static void rebuildCache() {
        synchronized (CACHE_LOCK) {
            List<SdmShopJeiEntry> entries = new ArrayList<>(ENTRY_CACHE);
            entries.sort(ENTRY_ORDER);
            cachedEntries = List.copyOf(entries);
        }
    }

    private static List<Object> collectRawEntries() {
        Set<Object> results = new LinkedHashSet<>();
        Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());

        for (String providerClassName : PROVIDER_CLASSES) {
            Class<?> providerClass = tryLoadClass(providerClassName);
            if (providerClass == null) {
                continue;
            }
            collectFromClass(providerClass, results, visited);
        }

        scanClasspathForCandidates(results, visited);

        return new ArrayList<>(results);
    }

    private static void collectFromClass(Class<?> providerClass, Set<Object> results, Set<Object> visited) {
        Object target = resolveTarget(providerClass);

        for (String methodName : ENTRY_METHODS) {
            invokeCandidate(providerClass, target, methodName).ifPresent(value -> flatten(value, results));
        }

        for (Method method : providerClass.getMethods()) {
            if (method.getParameterCount() != 0) {
                continue;
            }
            if (!Modifier.isPublic(method.getModifiers())) {
                continue;
            }
            String methodName = method.getName().toLowerCase(Locale.ROOT);
            if (!containsAny(methodName, "shop", "tab", "entry", "item", "list", "data", "manager", "registry", "api")) {
                continue;
            }
            invokeMethod(target, method).ifPresent(value -> flatten(value, results));
        }

        collectProviderFields(providerClass, target, results, visited);
    }

    private static void scanClasspathForCandidates(Set<Object> results, Set<Object> visited) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = SdmShopDataBridge.class.getClassLoader();
        }

        if (classLoader == null) {
            return;
        }

        Set<String> classNames = new LinkedHashSet<>();
        for (String prefix : SCAN_PREFIXES) {
            String path = prefix.replace('.', '/');
            try {
                Enumeration<URL> resources = classLoader.getResources(path);
                while (resources.hasMoreElements()) {
                    URL resource = resources.nextElement();
                    collectClassNamesFromResource(resource, prefix, classNames);
                }
            } catch (Exception ignored) {
            }
        }

        for (String className : classNames) {
            if (!containsAny(className.toLowerCase(Locale.ROOT), "shop", "tab", "entry", "item", "list", "data", "manager", "registry", "api", "plugin")) {
                continue;
            }

            Class<?> candidate = tryLoadClass(className);
            if (candidate != null) {
                collectFromClass(candidate, results, visited);
            }
        }
    }

    private static void collectClassNamesFromResource(URL resource, String prefix, Set<String> classNames) {
        String protocol = resource.getProtocol();
        if ("jar".equals(protocol)) {
            try {
                JarURLConnection connection = (JarURLConnection) resource.openConnection();
                try (JarFile jarFile = connection.getJarFile()) {
                    scanJarEntries(jarFile, prefix, classNames);
                }
            } catch (Exception ignored) {
            }
            return;
        }

        if ("file".equals(protocol)) {
            try {
                Path root = Path.of(resource.toURI());
                if (Files.isDirectory(root)) {
                    try (var paths = Files.walk(root)) {
                        paths.filter(path -> path.toString().endsWith(".class"))
                                .forEach(path -> addClassName(prefix, root, path, classNames));
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }

    private static void scanJarEntries(JarFile jarFile, String prefix, Set<String> classNames) {
        String prefixPath = prefix.replace('.', '/') + "/";
        for (JarEntry entry : Collections.list(jarFile.entries())) {
            String name = entry.getName();
            if (!name.startsWith(prefixPath) || !name.endsWith(".class")) {
                continue;
            }
            addClassNameFromPath(name, classNames);
        }
    }

    private static void addClassName(String prefix, Path root, Path path, Set<String> classNames) {
        Path relative = root.relativize(path);
        String name = relative.toString().replace('/', '.').replace('\\', '.');
        if (name.endsWith(".class")) {
            name = name.substring(0, name.length() - 6);
        }
        if (!name.isBlank()) {
            classNames.add(prefix + "." + name.replace('.', '/').replace('/', '.'));
        }
    }

    private static void addClassNameFromPath(String path, Set<String> classNames) {
        if (!path.endsWith(".class")) {
            return;
        }
        String className = path.substring(0, path.length() - 6).replace('/', '.').replace('\\', '.');
        classNames.add(className);
    }

    private static void collectProviderFields(Class<?> providerClass, Object target, Set<Object> results, Set<Object> visited) {
        collectFields(providerClass, null, results, visited);
        if (target != null) {
            collectFields(target.getClass(), target, results, visited);
        }
    }

    private static void collectFields(Class<?> type, Object target, Set<Object> results, Set<Object> visited) {
        if (type == null) {
            return;
        }

        for (Field field : type.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && target != null) {
                continue;
            }

            boolean interesting = isInterestingField(field);
            if (!interesting) {
                continue;
            }

            try {
                field.setAccessible(true);
                Object value = field.get(target);
                inspectValue(value, results, visited);
            } catch (ReflectiveOperationException ignored) {
            }
        }

        Class<?> parent = type.getSuperclass();
        if (parent != null && parent != Object.class) {
            collectFields(parent, target, results, visited);
        }
    }

    private static boolean isInterestingField(Field field) {
        String lower = field.getName().toLowerCase(Locale.ROOT);
        if (containsAny(lower, FIELD_NAMES.toArray(new String[0]))) {
            return true;
        }

        Class<?> fieldType = field.getType();
        return Collection.class.isAssignableFrom(fieldType)
                || Map.class.isAssignableFrom(fieldType)
                || Stream.class.isAssignableFrom(fieldType)
                || fieldType.isArray()
                || fieldType.getName().toLowerCase(Locale.ROOT).contains("shop")
                || fieldType.getName().toLowerCase(Locale.ROOT).contains("tab")
                || fieldType.getName().toLowerCase(Locale.ROOT).contains("entry");
    }

    private static boolean containsAny(String text, String... needles) {
        for (String needle : needles) {
            if (text.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private static void inspectValue(Object value, Set<Object> results, Set<Object> visited) {
        if (value == null || !visited.add(value)) {
            return;
        }

        flatten(value, results);

        if (value instanceof Map<?, ?> map) {
            map.values().forEach(entry -> inspectValue(entry, results, visited));
            return;
        }
        if (value instanceof Collection<?> collection) {
            collection.forEach(entry -> inspectValue(entry, results, visited));
            return;
        }
        if (value instanceof Stream<?> stream) {
            stream.forEach(entry -> inspectValue(entry, results, visited));
            return;
        }
        if (value.getClass().isArray()) {
            for (int i = 0; i < Array.getLength(value); i++) {
                inspectValue(Array.get(value, i), results, visited);
            }
            return;
        }

        String className = value.getClass().getName().toLowerCase(Locale.ROOT);
        if (className.contains("shop") || className.contains("tab") || className.contains("entry") || className.contains("item")) {
            collectNestedFields(value, results, visited);
        }
    }

    private static void collectNestedFields(Object value, Set<Object> results, Set<Object> visited) {
        Class<?> type = value.getClass();
        while (type != null && type != Object.class) {
            for (Field field : type.getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    Object nested = field.get(value);
                    if (nested != null && visited.add(nested)) {
                        inspectValue(nested, results, visited);
                    }
                } catch (ReflectiveOperationException ignored) {
                }
            }
            type = type.getSuperclass();
        }
    }

    private static SdmShopJeiEntry toEntry(Object raw) {
        ItemStack stack = extractItemStack(raw).orElse(ItemStack.EMPTY);
        if (stack.isEmpty()) {
            return null;
        }

        String shopName = extractString(raw, NAME_METHODS).orElse(stack.getDescriptionId());
        int quantity = extractInt(raw, QUANTITY_METHODS).orElse(1);
        Price price = extractPrice(raw).orElse(Price.EMPTY);
        boolean locked = extractBoolean(raw, LOCKED_METHODS).orElse(false);
        String questId = extractString(raw, QUEST_ID_METHODS).orElse("");
        if (!questId.isBlank()) {
            locked = !FtbQuestUtils.isQuestCompleted(questId);
        }
        boolean sell = extractBoolean(raw, List.of("isSell", "sell")).orElse(false);
        String lockReason = locked
                ? extractString(raw, LOCK_REASON_METHODS).orElseGet(() -> questId.isBlank() ? "gui.shopjei.lock_info" : questId)
                : "";

        return new SdmShopJeiEntry(stack, price, quantity, shopName, DEFAULT_CURRENCY, sell, locked, lockReason);
    }

    private static Optional<ItemStack> extractItemStack(Object raw) {
        Optional<Object> stackObject = invokeAny(raw, STACK_METHODS);
        if (stackObject.isPresent() && stackObject.get() instanceof ItemStack stack) {
            return Optional.of(stack);
        }

        for (String name : List.of("itemStack", "stack", "item", "output", "result", "sellItem", "displayItem")) {
            Optional<Object> fieldValue = readField(raw, name);
            if (fieldValue.isPresent() && fieldValue.get() instanceof ItemStack stack) {
                return Optional.of(stack);
            }
        }

        return Optional.empty();
    }

    private static Optional<Price> extractPrice(Object raw) {
        Optional<Object> priceObject = invokeAny(raw, PRICE_METHODS);
        Optional<Price> price = toPrice(priceObject.orElse(raw));
        if (price.isPresent()) {
            return price;
        }

        for (String name : List.of("entryPrice", "price", "cost", "basicPrice", "addPrice", "entryCost")) {
            Optional<Object> fieldValue = readField(raw, name);
            if (fieldValue.isPresent()) {
                Optional<Price> fromField = toPrice(fieldValue.get());
                if (fromField.isPresent()) {
                    return fromField;
                }
            }
        }

        return toPrice(raw);
    }

    private static Optional<Price> toPrice(Object candidate) {
        if (candidate == null) {
            return Optional.empty();
        }

        if (candidate instanceof Number number) {
            int value = Math.max(0, number.intValue());
            return Optional.of(Price.of(value, 0, 0));
        }

        Integer total = extractInt(candidate, List.of("totalPrice", "getTotalPrice", "amount", "value")).orElse(null);
        Integer basic = extractInt(candidate, List.of("getBasicPrice", "basicPrice", "getPrice", "price", "getCost", "cost")).orElse(total);
        Integer add = extractInt(candidate, List.of("getAddPrice", "addPrice", "getExtraPrice", "extraPrice")).orElse(0);
        Integer reputation = extractInt(candidate, List.of("getReputation", "reputation")).orElse(0);

        if (basic == null) {
            return Optional.empty();
        }

        return Optional.of(Price.of(Math.max(0, basic), Math.max(0, add), reputation));
    }

    private static Optional<String> extractString(Object raw, List<String> names) {
        Optional<Object> value = invokeAny(raw, names);
        if (value.isPresent()) {
            Object rawValue = value.get();
                    if (rawValue instanceof String string && !string.isBlank()) {
                        return Optional.of(string);
                    }
                    return Optional.of(rawValue.toString());
        }

        for (String name : names) {
            Optional<Object> fieldValue = readField(raw, name);
            if (fieldValue.isPresent()) {
                return Optional.of(fieldValue.get().toString());
            }
        }

        return Optional.empty();
    }

    private static Optional<Integer> extractInt(Object raw, List<String> names) {
        Optional<Object> value = invokeAny(raw, names);
        if (value.isPresent() && value.get() instanceof Number number) {
            return Optional.of(number.intValue());
        }

        for (String name : names) {
            Optional<Object> fieldValue = readField(raw, name);
            if (fieldValue.isPresent() && fieldValue.get() instanceof Number number) {
                return Optional.of(number.intValue());
            }
        }

        return Optional.empty();
    }

    private static Optional<Boolean> extractBoolean(Object raw, List<String> names) {
        Optional<Object> value = invokeAny(raw, names);
        if (value.isPresent() && value.get() instanceof Boolean bool) {
            return Optional.of(bool);
        }

        for (String name : names) {
            Optional<Object> fieldValue = readField(raw, name);
            if (fieldValue.isPresent() && fieldValue.get() instanceof Boolean bool) {
                return Optional.of(bool);
            }
        }

        return Optional.empty();
    }

    private static Optional<Object> invokeAny(Object target, List<String> names) {
        for (String name : names) {
            Optional<Object> value = invokeNoArg(target, name);
            if (value.isPresent()) {
                return value;
            }
        }
        return Optional.empty();
    }

    private static Optional<Object> invokeCandidate(Class<?> providerClass, Object target, String methodName) {
        for (Method method : providerClass.getMethods()) {
            if (!method.getName().equals(methodName) || method.getParameterCount() != 0) {
                continue;
            }
            if (!Modifier.isStatic(method.getModifiers()) && target == null) {
                continue;
            }
            return invokeMethod(target, method);
        }
        return Optional.empty();
    }

    private static Optional<Object> invokeMethod(Object target, Method method) {
        try {
            Object value = method.invoke(Modifier.isStatic(method.getModifiers()) ? null : target);
            return Optional.ofNullable(value);
        } catch (ReflectiveOperationException | IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    private static Optional<Object> invokeNoArg(Object target, String methodName) {
        if (target == null) {
            return Optional.empty();
        }

        for (Method method : target.getClass().getMethods()) {
            if (!method.getName().equals(methodName) || method.getParameterCount() != 0) {
                continue;
            }
            try {
                return Optional.ofNullable(method.invoke(target));
            } catch (ReflectiveOperationException | IllegalArgumentException ignored) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private static Optional<Object> readField(Object raw, String fieldName) {
        if (raw == null) {
            return Optional.empty();
        }

        Class<?> type = raw.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField(fieldName);
                field.setAccessible(true);
                return Optional.ofNullable(field.get(raw));
            } catch (ReflectiveOperationException ignored) {
                type = type.getSuperclass();
            }
        }
        return Optional.empty();
    }

    private static Optional<Object> invokeSingleArg(Object target, String methodName, Object argument) {
        if (target == null) {
            return Optional.empty();
        }

        for (Method method : target.getClass().getMethods()) {
            if (!method.getName().equals(methodName) || method.getParameterCount() != 1) {
                continue;
            }
            Class<?> parameterType = method.getParameterTypes()[0];
            if (!isCompatible(parameterType, argument)) {
                continue;
            }
            try {
                return Optional.ofNullable(method.invoke(target, argument));
            } catch (ReflectiveOperationException | IllegalArgumentException ignored) {
                return Optional.empty();
            }
        }
        return Optional.empty();
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

    private static void flatten(Object value, Set<Object> results) {
        if (value == null) {
            return;
        }
        if (value instanceof Collection<?> collection) {
            collection.stream().filter(Objects::nonNull).forEach(results::add);
            return;
        }
        if (value instanceof Stream<?> stream) {
            stream.filter(Objects::nonNull).forEach(results::add);
            return;
        }
        if (value.getClass().isArray()) {
            for (int i = 0; i < Array.getLength(value); i++) {
                Object element = Array.get(value, i);
                if (element != null) {
                    results.add(element);
                }
            }
            return;
        }
        results.add(value);
    }

    private static Class<?> tryLoadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }

    private static Optional<Object> tryInstantiateEntry(Class<?> entryClass, Object shopTab) {
        for (var constructor : entryClass.getDeclaredConstructors()) {
            try {
                constructor.setAccessible(true);
                if (constructor.getParameterCount() == 0) {
                    return Optional.of(constructor.newInstance());
                }
                if (constructor.getParameterCount() == 1) {
                    Class<?> parameterType = constructor.getParameterTypes()[0];
                    if (isCompatible(parameterType, shopTab)) {
                        return Optional.of(constructor.newInstance(shopTab));
                    }
                }
            } catch (ReflectiveOperationException | IllegalArgumentException ignored) {
            }
        }

        for (String factoryName : List.of("create", "of", "from", "instance", "getInstance")) {
            Optional<Object> value = invokeCandidate(entryClass, null, factoryName);
            if (value.isPresent() && entryClass.isInstance(value.get())) {
                return value;
            }
        }

        return Optional.empty();
    }

    private static Object resolveTarget(Class<?> providerClass) {
        try {
            Field instance = providerClass.getDeclaredField("INSTANCE");
            instance.setAccessible(true);
            Object value = instance.get(null);
            if (value != null) {
                return value;
            }
        } catch (ReflectiveOperationException ignored) {
        }

        for (String methodName : List.of("getInstance", "instance", "get", "create")) {
            try {
                Method method = providerClass.getDeclaredMethod(methodName);
                method.setAccessible(true);
                Object value = method.invoke(null);
                if (value != null) {
                    return value;
                }
            } catch (ReflectiveOperationException ignored) {
            }
        }

        return null;
    }
}





