package com.list.all;

import com.list.client.screen.ForestryHybridizerScreen;
import com.list.client.screen.ForestryGreenhouseScreen;
import com.list.client.screen.FishPondScreen;
import com.list.client.screen.SellingBinScreen;
import com.list.menu.ForestryGreenhouseMenu;
import com.list.menu.ForestryHybridizerMenu;
import com.list.menu.FishPondMenu;
import com.list.menu.SellingBinMenu;
import com.tterrag.registrate.util.entry.MenuEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraftforge.network.NetworkHooks;

import static com.list.ListMod.REGISTRATE;

public class ModMenus {
    @SuppressWarnings("DataFlowIssue")
    public static final MenuEntry<FishPondMenu> FISH_POND = REGISTRATE
        .menu("fish_pond", FishPondMenu::new, () -> FishPondScreen::new)
        .register();

    @SuppressWarnings("DataFlowIssue")
    public static final MenuEntry<ForestryHybridizerMenu> FORESTRY_HYBRIDIZER = REGISTRATE
        .menu("forestry_hybridizer", ForestryHybridizerMenu::new, () -> ForestryHybridizerScreen::new)
        .register();

    @SuppressWarnings("DataFlowIssue")
    public static final MenuEntry<ForestryGreenhouseMenu> FORESTRY_GREENHOUSE = REGISTRATE
        .menu("forestry_greenhouse", ForestryGreenhouseMenu::new, () -> ForestryGreenhouseScreen::new)
        .register();

    @SuppressWarnings("DataFlowIssue")
    public static final MenuEntry<SellingBinMenu> SELLING_BIN = REGISTRATE
        .menu("selling_bin", SellingBinMenu::new, () -> SellingBinScreen::new)
        .register();

    public static void open(ServerPlayer player, MenuProvider provider) {
        NetworkHooks.openScreen(player, provider);
    }

    public static void open(ServerPlayer player, MenuProvider provider, BlockPos pos) {
        NetworkHooks.openScreen(player, provider, pos);
    }

    public static void register() {
    }
}
