package com.y271727uy.shopcore.core.consumer.common.lifecycle;

import com.y271727uy.shopcore.api.component.consumer.ConsumerOwnershipTarget;
import com.y271727uy.shopcore.core.consumer.common.entity.EntityConsumerActor;
import com.y271727uy.shopcore.core.consumer.common.entity.EntityPersistentConsumerMemory;
import com.y271727uy.shopcore.core.consumer.common.entity.ManualMobConsumerNavigator;
import com.y271727uy.shopcore.core.order.prompt.overhead.CustomerOverheadOrderPromptService;
import com.y271727uy.shopcore.core.consumer.common.wait.QueueConsumerPhase;
import com.y271727uy.shopcore.core.order.taking.EntityOrderTakingMemory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

/** Persisted departure handoff, independent of the removed shop and optional NPC mods. */
public final class OrphanConsumerDepartureService {
    private static final String ROOT_TAG = "ShopCoreConsumer";
    private static final String ORPHAN_TAG = "OrphanDeparture";
    private static final String TARGET_TAG = "DepartureTarget";
    private static final EntityPersistentConsumerMemory MEMORY = new EntityPersistentConsumerMemory();
    private static final EntityOrderTakingMemory ORDER_TIMING = new EntityOrderTakingMemory();
    private static final ManualMobConsumerNavigator NAVIGATOR = new ManualMobConsumerNavigator();

    private OrphanConsumerDepartureService() { }

    public static void tick(ServerLevel level) {
        for (Entity entity : level.getEntities().getAll()) {
            if (!(entity instanceof Mob mob) || mob.isRemoved()) {
                continue;
            }
            CompoundTag data = mob.getPersistentData().getCompound(ROOT_TAG);
            EntityConsumerActor actor = new EntityConsumerActor(mob);
            if (!data.getBoolean(ORPHAN_TAG)) {
                var shopPos = MEMORY.shopPos(actor);
                // A missing chunk is not evidence of a removed shop. Never load it here.
                if (shopPos.isEmpty() || !level.hasChunkAt(shopPos.get())) {
                    continue;
                }
                var owner = level.getBlockEntity(shopPos.get());
                if (!ConsumerOwnershipTarget.shouldDetach(true,
                        owner instanceof ConsumerOwnershipTarget target ? target : null, mob.getUUID())) {
                    continue;
                }
                // The block is gone, was replaced, or no longer owns this consumer.
                MEMORY.setOrphanDeparture(actor, true);
                MEMORY.setPhase(actor, QueueConsumerPhase.LEAVING);
                ORDER_TIMING.clear(actor);
            }

            MEMORY.clearShopPos(actor);
            MEMORY.clearOrderId(actor);
            CustomerOverheadOrderPromptService.clear(mob);
            if (!data.contains(TARGET_TAG)) {
                MEMORY.setDepartureTarget(actor, NAVIGATOR.prepareDeparture(actor, 5.0D));
            }
            CompoundTag savedTarget = data.getCompound(TARGET_TAG);
            Vec3 target = new Vec3(savedTarget.getDouble("X"), savedTarget.getDouble("Y"), savedTarget.getDouble("Z"));
            OrphanDepartureStep step = OrphanDepartureStep.towards(mob.position(), target);
            if (step.finished()) {
                level.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,
                        mob.getX(), mob.getY() + 0.25D, mob.getZ(), 15, 0.1D, 0.2D, 0.1D, 0.05D);
                data.remove(ORPHAN_TAG);
                mob.discard();
                continue;
            }

            mob.setNoAi(true);
            Vec3 next = step.position();
            float yaw = step.yaw();
            mob.moveTo(next.x, next.y, next.z, yaw, mob.getXRot());
            mob.setYHeadRot(yaw);
            mob.setYBodyRot(yaw);
        }
    }
}
