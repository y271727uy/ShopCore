package com.y271727uy.shopcore.event;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.y271727uy.shopcore.ShopcoreMod;
import com.y271727uy.shopcore.block.entity.SellingBinBlockEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ShopcoreMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CommandEvents {
    private CommandEvents() {
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getServer().getCommands().getDispatcher();
        
        dispatcher.register(Commands.literal("shopcore")
                .then(Commands.literal("sellingbin")
                        .then(Commands.literal("sell")
                                .requires(source -> source.hasPermission(2)) // OP level 2 required
                                .executes(CommandEvents::executeSellCommand)
                        )
                )
        );
    }

    private static int executeSellCommand(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        
        int processedCount = 0;
        
        // Iterate through registered loaded SellingBinBlockEntity instances
        for (SellingBinBlockEntity sellingBin : SellingBinBlockEntity.getLoadedInstances()) {
            if (sellingBin.getLevel() == level) {
                sellingBin.runAllRecipesBroadcast(level);
                processedCount++;
            }
        }
        
        // Send feedback message
        final int resultCount = processedCount;
        if (resultCount > 0) {
            source.sendSuccess(() -> Component.literal("§aExecuted recipes on " + resultCount + " selling bin(s)"), true);
        } else {
            source.sendSuccess(() -> Component.literal("§cNo selling bins found"), true);
        }
        
        return processedCount;
    }
}

