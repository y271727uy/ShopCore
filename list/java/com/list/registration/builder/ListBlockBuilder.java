package com.list.registration.builder;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.list.registration.ListRegistrate;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.AbstractBuilder;
import com.tterrag.registrate.builders.BlockEntityBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.providers.loot.RegistrateBlockLootTables;
import com.tterrag.registrate.providers.loot.RegistrateLootTableProvider;
import com.tterrag.registrate.util.OneTimeEventReceiver;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullBiFunction;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ListBlockBuilder<T extends Block, P> extends AbstractBuilder<Block, T, P, ListBlockBuilder<T, P>> {

    public static <T extends Block, P> ListBlockBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, NonNullFunction<BlockBehaviour.Properties, T> factory) {
        return new ListBlockBuilder<>(owner, parent, name, callback, factory, BlockBehaviour.Properties::of)
            .defaultBlockstate().defaultLoot().defaultLang();
    }

    private final NonNullFunction<BlockBehaviour.Properties, T> factory;

    private NonNullSupplier<BlockBehaviour.Properties> initialProperties;
    private NonNullFunction<BlockBehaviour.Properties, BlockBehaviour.Properties> propertiesCallback = NonNullUnaryOperator.identity();
    private List<Supplier<Supplier<RenderType>>> renderLayers = new ArrayList<>(1);

    @Nullable
    private NonNullSupplier<Supplier<BlockColor>> colorHandler;
    public ListBlockBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, NonNullFunction<BlockBehaviour.Properties, T> factory, NonNullSupplier<BlockBehaviour.Properties> initialProperties) {
        super(owner, parent, name, callback, ForgeRegistries.Keys.BLOCKS);

        this.factory = factory;
        this.initialProperties = initialProperties;
    }

    public ListBlockBuilder<T, P> properties(NonNullUnaryOperator<BlockBehaviour.Properties> func) {
        propertiesCallback = propertiesCallback.andThen(func);
        return this;
    }

    public ListBlockBuilder<T, P> initialProperties(NonNullSupplier<? extends Block> block) {
        initialProperties = () -> BlockBehaviour.Properties.copy(block.get());
        return this;
    }

    @Deprecated(forRemoval = true)
    public ListBlockBuilder<T, P> addLayer(Supplier<Supplier<RenderType>> layer) {
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            Preconditions.checkArgument(RenderType.chunkBufferLayers().contains(layer.get().get()), "Invalid block layer: " + layer);
        });
        if (this.renderLayers.isEmpty()) {
            onRegister(this::registerLayers);
        }
        this.renderLayers.add(layer);
        return this;
    }

    @SuppressWarnings("deprecation")
    protected void registerLayers(T entry) {
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            OneTimeEventReceiver.addModListener(getOwner(), FMLClientSetupEvent.class, $ -> {
                if (renderLayers.size() == 1) {
                    final RenderType layer = renderLayers.get(0).get().get();
                    ItemBlockRenderTypes.setRenderLayer(entry, layer);
                } else if (renderLayers.size() > 1) {
                    final Set<RenderType> layers = renderLayers.stream()
                        .map(s -> s.get().get())
                        .collect(Collectors.toSet());
                    ItemBlockRenderTypes.setRenderLayer(entry, layers::contains);
                }
            });
        });
    }

    public ListBlockBuilder<T, P> simpleItem() {
        return item().build();
    }

    public ListItemBuilder<BlockItem, ListBlockBuilder<T, P>> item() {
        return item(BlockItem::new);
    }

    public <I extends Item> ListItemBuilder<I, ListBlockBuilder<T, P>> item(NonNullBiFunction<? super T, Item.Properties, ? extends I> factory) {
        final var sup = asSupplier();
        return ((ListRegistrate)getOwner()).<I, ListBlockBuilder<T, P>> listItem(this, getName(), p -> factory.apply(getEntry(), p))
            .setData(ProviderType.LANG, NonNullBiConsumer.noop())
            .model((ctx, prov) -> {
                Optional<String> model = getOwner().getDataProvider(ProviderType.BLOCKSTATE)
                    .flatMap(p -> p.getExistingVariantBuilder(getEntry()))
                    .map(b -> b.getModels().get(b.partialState()))
                    .map(BlockStateProvider.ConfiguredModelList::toJSON)
                    .filter(JsonElement::isJsonObject)
                    .map(j -> j.getAsJsonObject().get("model"))
                    .map(JsonElement::getAsString);
                if (model.isPresent()) {
                    prov.withExistingParent(ctx.getName(), model.get());
                } else {
                    prov.blockItem(sup);
                }
            });
    }

    public <BE extends BlockEntity> ListBlockBuilder<T, P> simpleBlockEntity(BlockEntityBuilder.BlockEntityFactory<BE> factory) {
        return blockEntity(factory).build();
    }

    public <BE extends BlockEntity> BlockEntityBuilder<BE, ListBlockBuilder<T, P>> blockEntity(BlockEntityBuilder.BlockEntityFactory<BE> factory) {
        return getOwner().blockEntity(this, getName(), factory).validBlock(asSupplier());
    }

    public ListBlockBuilder<T, P> color(NonNullSupplier<Supplier<BlockColor>> colorHandler) {
        if (this.colorHandler == null) {
            DistExecutor.runWhenOn(Dist.CLIENT, () -> this::registerBlockColor);
        }
        this.colorHandler = colorHandler;
        return this;
    }

    protected void registerBlockColor() {
        OneTimeEventReceiver.addModListener(getOwner(), RegisterColorHandlersEvent.Block.class, e -> {
            NonNullSupplier<Supplier<BlockColor>> colorHandler = this.colorHandler;
            if (colorHandler != null) {
                e.register(colorHandler.get().get(), getEntry());
            }
        });
    }

    public ListBlockBuilder<T, P> defaultBlockstate() {
        return blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry()));
    }

    public ListBlockBuilder<T, P> blockstate(NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> cons) {
        return setData(ProviderType.BLOCKSTATE, cons);
    }

    public ListBlockBuilder<T, P> defaultLang() {
        return lang(Block::getDescriptionId);
    }

    public ListBlockBuilder<T, P> lang(String name) {
        return lang(Block::getDescriptionId, name);
    }

    public ListBlockBuilder<T, P> defaultLoot() {
        return loot(RegistrateBlockLootTables::dropSelf);
    }

    public ListBlockBuilder<T, P> loot(NonNullBiConsumer<RegistrateBlockLootTables, T> cons) {
        return setData(ProviderType.LOOT, (ctx, prov) -> prov.addLootAction(RegistrateLootTableProvider.LootType.BLOCK, tb -> {
            if (!ctx.getEntry().getLootTable().equals(BuiltInLootTables.EMPTY)) {
                cons.accept(tb, ctx.getEntry());
            }
        }));
    }

    public ListBlockBuilder<T, P> recipe(NonNullBiConsumer<DataGenContext<Block, T>, RegistrateRecipeProvider> cons) {
        return setData(ProviderType.RECIPE, cons);
    }

    @SafeVarargs
    public final ListBlockBuilder<T, P> tag(TagKey<Block>... tags) {
        return tag(ProviderType.BLOCK_TAGS, tags);
    }

    @Override
    protected T createEntry() {
        @Nonnull BlockBehaviour.Properties properties = this.initialProperties.get();
        properties = propertiesCallback.apply(properties);
        return factory.apply(properties);
    }

    @Override
    protected RegistryEntry<T> createEntryWrapper(RegistryObject<T> delegate) {
        return new BlockEntry<>(getOwner(), delegate);
    }

    @Override
    public BlockEntry<T> register() {
        return (BlockEntry<T>) super.register();
    }
}
