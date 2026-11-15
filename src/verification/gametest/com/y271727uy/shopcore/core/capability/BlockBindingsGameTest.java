package com.y271727uy.shopcore.core.capability;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.RegisterEvent;

import java.util.List;
import java.util.Set;

/** Compiled only with block-bindings-gametest.init.gradle. No production block is bound by default. */
@GameTestHolder("shopcore")
@PrefixGameTestTemplate(false)
@Mod.EventBusSubscriber(modid = "shopcore", bus = Mod.EventBusSubscriber.Bus.MOD)
public final class BlockBindingsGameTest {
    private static final BlockPos A = new BlockPos(1, 1, 1);
    private static final BlockPos B = new BlockPos(3, 1, 1);
    private static final BlockEntityType<Probe> PROBE = new BlockEntityType<>(Probe::new, Set.of(), null);
    private static final Capability<Counter> COUNT = CapabilityManager.get(new CapabilityToken<>() { });

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(Registries.BLOCK_ENTITY_TYPE, id("binding_probe"), () -> PROBE);
    }

    @SubscribeEvent
    public static void capabilities(RegisterCapabilitiesEvent event) { event.register(Counter.class); }

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            BlockBindingRegistry.bindBlockEntity(Blocks.HAY_BLOCK, () -> PROBE,
                    (level, pos, state, entity) -> entity.ticks++, null);
            BlockBindingRegistry.bindBlockEntity(Blocks.GOLD_BLOCK, () -> PROBE,
                    (level, pos, state, entity) -> entity.ticks++, null);
            for (var block : List.of(Blocks.HAY_BLOCK, Blocks.GOLD_BLOCK, Blocks.DIAMOND_BLOCK, Blocks.CHEST)) {
                BlockBindingRegistry.bindCapability(block, id("binding_count"), COUNT, owner -> {
                    if (owner instanceof Probe probe && !probe.constructed) {
                        throw new AssertionError("Capability factory ran before subclass construction finished");
                    }
                    return new Counter(owner);
                }, Counter::save, Counter::load, side -> side == null || side == Direction.NORTH);
            }
            expectFailure(() -> BlockBindingRegistry.bindBlockEntity(Blocks.HAY_BLOCK, () -> PROBE, null, null));
            expectFailure(() -> BlockBindingRegistry.bindBlockEntity(Blocks.CHEST, () -> PROBE, null, null));
            expectFailure(() -> BlockBindingRegistry.bindBlockEntity(Blocks.AIR, () -> PROBE, null, null));
            expectFailure(() -> BlockBindingRegistry.bindCapability(Blocks.GOLD_BLOCK, id("duplicate"), COUNT,
                    Counter::new, Counter::save, Counter::load, side -> true));
        });
    }

    @GameTest(template = "binding_empty", timeoutTicks = 100)
    public static void createsIndependentEntitiesAndTicks(GameTestHelper helper) {
        helper.setBlock(A, Blocks.HAY_BLOCK);
        helper.setBlock(B, Blocks.GOLD_BLOCK);
        Probe first = probe(helper, A);
        Probe second = probe(helper, B);
        helper.assertTrue(first != second, "Each location must own a separate BlockEntity");
        helper.assertTrue(first.getBlockPos().equals(helper.absolutePos(A)), "Wrong host position");
        helper.assertTrue(PROBE.isValid(first.getBlockState()), "Bound type validation failed");
        helper.assertTrue(!PROBE.isValid(Blocks.STONE.defaultBlockState()), "Type leaked onto unbound blocks");
        count(first).set(41);
        helper.assertTrue(count(second).value == 0, "Capabilities leaked between locations");
        helper.runAfterDelay(10, () -> {
            helper.assertTrue(first.ticks > 0 && second.ticks > 0, "Bound tickers did not execute");
            helper.succeed();
        });
    }

    @GameTest(template = "binding_empty")
    public static void stateChangesPreserveInstance(GameTestHelper helper) {
        helper.setBlock(A, Blocks.HAY_BLOCK);
        Probe first = probe(helper, A);
        count(first).set(17);
        helper.setBlock(A, Blocks.HAY_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
        helper.assertTrue(helper.getBlockEntity(A) == first, "State-only changes replaced the instance");
        helper.assertTrue(first.getBlockState().getValue(RotatedPillarBlock.AXIS) == Direction.Axis.X, "Stale host state");
        helper.assertTrue(count(first).value == 17, "State change lost capability data");
        helper.succeed();
    }

    @GameTest(template = "binding_empty")
    public static void removalInvalidatesAndReplacementStartsFresh(GameTestHelper helper) {
        helper.setBlock(A, Blocks.GOLD_BLOCK);
        Probe old = probe(helper, A);
        var handle = old.getCapability(COUNT);
        count(old).set(99);
        helper.setBlock(A, Blocks.AIR);
        helper.assertTrue(old.isRemoved() && !handle.isPresent(), "Removed capability handle remained valid");
        helper.setBlock(A, Blocks.GOLD_BLOCK);
        helper.assertTrue(helper.getBlockEntity(A) != old && count(helper.getBlockEntity(A)).value == 0,
                "Same-tick replacement reused old identity or state");
        BlockEntity replacement = helper.getBlockEntity(A);
        helper.setBlock(A, Blocks.HAY_BLOCK);
        helper.assertTrue(helper.getBlockEntity(A) != replacement && replacement.isRemoved(),
                "Different blocks sharing one entity type reused the previous instance");
        helper.setBlock(A, Blocks.STONE);
        helper.assertTrue(helper.getBlockEntity(A) == null, "Entity leaked after replacing with unbound block");
        helper.succeed();
    }

    @GameTest(template = "binding_empty")
    public static void persistenceAndPendingRestore(GameTestHelper helper) {
        helper.setBlock(A, Blocks.GOLD_BLOCK);
        Probe old = probe(helper, A);
        old.value = 123;
        count(old).set(456);
        CompoundTag saved = old.saveWithFullMetadata();
        var handle = old.getCapability(COUNT);
        old.onChunkUnloaded();
        helper.assertTrue(!handle.isPresent(), "Unload did not invalidate capability handles");
        BlockPos pos = helper.absolutePos(A);
        LevelChunk chunk = helper.getLevel().getChunkAt(pos);
        chunk.removeBlockEntity(pos);
        chunk.setBlockEntityNbt(saved);
        BlockEntity restored = chunk.getBlockEntity(pos, LevelChunk.EntityCreationType.IMMEDIATE);
        helper.assertTrue(restored instanceof Probe && restored != old, "NBT did not restore registered BE type");
        helper.assertTrue(((Probe) restored).value == 123 && count(restored).value == 456,
                "Entity or capability NBT was lost");
        helper.succeed();
    }

    @GameTest(template = "binding_empty")
    public static void capabilityOnlyAndNativeHost(GameTestHelper helper) {
        helper.setBlock(A, Blocks.DIAMOND_BLOCK);
        helper.setBlock(B, Blocks.CHEST);
        BlockEntity carrier = helper.getBlockEntity(A);
        BlockEntity chest = helper.getBlockEntity(B);
        helper.assertTrue(carrier != null && carrier.getType() == BlockBindingHooks.carrierType(), "Missing capability carrier");
        helper.assertTrue(chest instanceof ChestBlockEntity, "Native BlockEntity was replaced");
        helper.assertTrue(chest.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent(), "Native capability was hidden");
        helper.assertTrue(carrier.getCapability(COUNT, Direction.NORTH).isPresent(), "Supported side unavailable");
        helper.assertTrue(!carrier.getCapability(COUNT, Direction.SOUTH).isPresent(), "Side filter ignored");
        count(carrier).set(15);
        count(chest).set(30);
        BlockEntity copy = BlockEntity.loadStatic(carrier.getBlockPos(), carrier.getBlockState(), carrier.saveWithFullMetadata());
        helper.assertTrue(copy != null && count(copy).value == 15, "Capability-only host did not persist");
        helper.assertTrue(count(chest).value == 30, "Native host attachment failed");
        helper.succeed();
    }

    @GameTest(template = "binding_empty")
    public static void dummyNbtAndImmediateCreation(GameTestHelper helper) {
        helper.setBlock(A, Blocks.GOLD_BLOCK);
        BlockPos pos = helper.absolutePos(A);
        LevelChunk chunk = helper.getLevel().getChunkAt(pos);
        chunk.removeBlockEntity(pos);
        CompoundTag dummy = new CompoundTag();
        dummy.putString("id", "DUMMY");
        dummy.putInt("x", pos.getX());
        dummy.putInt("y", pos.getY());
        dummy.putInt("z", pos.getZ());
        chunk.setBlockEntityNbt(dummy);
        helper.assertTrue(chunk.getBlockEntity(pos) instanceof Probe, "DUMMY promotion failed");
        chunk.removeBlockEntity(pos);
        helper.assertTrue(chunk.getBlockEntity(pos, LevelChunk.EntityCreationType.IMMEDIATE) instanceof Probe,
                "Immediate creation failed");
        helper.succeed();
    }

    @GameTest(template = "binding_empty")
    public static void worldGenerationCreationPaths(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(A);
        LevelChunk chunk = level.getChunkAt(pos);
        var fullRegion = new WorldGenRegion(level, List.of(chunk), ChunkStatus.FULL, 0);
        fullRegion.setBlock(pos, Blocks.GOLD_BLOCK.defaultBlockState(), 3, 0);
        helper.assertTrue(fullRegion.getBlockEntity(pos) instanceof Probe, "Full worldgen creation failed");
        var proto = new ProtoChunk(chunk.getPos(), UpgradeData.EMPTY, level,
                level.registryAccess().registryOrThrow(Registries.BIOME), null);
        proto.setStatus(ChunkStatus.FEATURES);
        var protoRegion = new WorldGenRegion(level, List.of(proto), ChunkStatus.FEATURES, 0);
        protoRegion.setBlock(pos, Blocks.GOLD_BLOCK.defaultBlockState(), 3, 0);
        BlockEntity generated = protoRegion.getBlockEntity(pos);
        helper.assertTrue(generated instanceof Probe, "Proto worldgen DUMMY creation failed");
        var handle = generated.getCapability(COUNT);
        protoRegion.setBlock(pos, Blocks.DIAMOND_BLOCK.defaultBlockState(), 3, 0);
        helper.assertTrue(generated.isRemoved() && !handle.isPresent(), "Proto replacement retained old host");
        helper.assertTrue(protoRegion.getBlockEntity(pos).getType() == BlockBindingHooks.carrierType(),
                "Proto replacement did not create the new host type");
        BlockEntity carrier = protoRegion.getBlockEntity(pos);
        var carrierHandle = carrier.getCapability(COUNT);
        protoRegion.setBlock(pos, Blocks.STONE.defaultBlockState(), 3, 0);
        helper.assertTrue(carrier.isRemoved() && !carrierHandle.isPresent() && protoRegion.getBlockEntity(pos) == null,
                "Proto replacement with an unbound block did not invalidate the carrier");
        helper.succeed();
    }

    @GameTest(template = "binding_empty")
    public static void registrationIsFrozenAndUnboundBlocksUnaffected(GameTestHelper helper) {
        expectFailure(() -> BlockBindingRegistry.bindBlockEntity(Blocks.STONE, () -> PROBE, null, null));
        helper.setBlock(A, Blocks.STONE);
        helper.assertTrue(!helper.getBlockState(A).hasBlockEntity() && helper.getBlockEntity(A) == null,
                "Unbound blocks acquired a BlockEntity");
        helper.succeed();
    }

    private static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath("shopcore", path); }
    private static Counter count(BlockEntity owner) { return owner.getCapability(COUNT).orElseThrow(AssertionError::new); }
    private static Probe probe(GameTestHelper helper, BlockPos pos) {
        helper.assertTrue(helper.getBlockEntity(pos) instanceof Probe, "Wrong bound BlockEntity at " + pos);
        return (Probe) helper.getBlockEntity(pos);
    }

    private static void expectFailure(Runnable operation) {
        try { operation.run(); }
        catch (IllegalArgumentException | IllegalStateException expected) { return; }
        throw new AssertionError("Invalid registration was accepted");
    }

    private static final class Probe extends BlockEntity {
        private boolean constructed;
        private int ticks;
        private int value;
        private Probe(BlockPos pos, BlockState state) { super(PROBE, pos, state); constructed = true; }
        @Override protected void saveAdditional(CompoundTag tag) { super.saveAdditional(tag); tag.putInt("ProbeValue", value); }
        @Override public void load(CompoundTag tag) { super.load(tag); value = tag.getInt("ProbeValue"); }
    }

    private static final class Counter {
        private final BlockEntity owner;
        private int value;
        private Counter(BlockEntity owner) { this.owner = owner; }
        private void set(int value) { this.value = value; owner.setChanged(); }
        private CompoundTag save() { var tag = new CompoundTag(); tag.putInt("Value", value); return tag; }
        private void load(CompoundTag tag) { value = tag.getInt("Value"); }
    }
}
