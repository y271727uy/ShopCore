package com.list.all;

import com.list.ListMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import static com.list.ListMod.REGISTRATE;

public class ModCreativeModeTabs {

    // 创建 DeferredRegister
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ListMod.MODID);

    // 主标签页
    public static final RegistryObject<CreativeModeTab> LIST_TAB = CREATIVE_TABS.register("list", () ->
            CreativeModeTab.builder()
                    .title(REGISTRATE.addLang("itemGroup", ListMod.rl("list"), "List"))  // 语言键: itemGroup.listmod.list
                    .icon(() -> new ItemStack(ModItems.INDUSTRIAL_OSMIUM_CREDIT.get()))
                    .displayItems((parameters, output) -> {

                    output.accept(ModItems.COPPER_GT_CREDIT.get());
                    output.accept(ModItems.CUPRONICKEL_GT_CREDIT.get());
                    output.accept(ModItems.SILVER_GT_CREDIT.get());
                    output.accept(ModItems.GOLD_GT_CREDIT.get());
                    output.accept(ModItems.PLATINUM_GT_CREDIT.get());
                    output.accept(ModItems.OSMIUM_GT_CREDIT.get());
                    output.accept(ModItems.NAQUADAH_GT_CREDIT.get());
                    output.accept(ModItems.NEUTRONIUM_GT_CREDIT.get());
                    output.accept(ModItems.DOGE_COIN.get());
                    output.accept(ModItems.INDUSTRIAL_COPPER_CREDIT.get());
                    output.accept(ModItems.INDUSTRIAL_SILVER_CREDIT.get());
                    output.accept(ModItems.INDUSTRIAL_GOLD_CREDIT.get());
                    output.accept(ModItems.INDUSTRIAL_PLATINUM_CREDIT.get());
                    output.accept(ModItems.INDUSTRIAL_COPPER_CREDIT.get());
                    output.accept(ModItems.INDUSTRIAL_SILVER_CREDIT.get());
                    output.accept(ModItems.INDUSTRIAL_GOLD_CREDIT.get());
                    output.accept(ModItems.INDUSTRIAL_PLATINUM_CREDIT.get());
                    output.accept(ModItems.INDUSTRIAL_OSMIUM_CREDIT.get());
                    output.accept(ModItems.JUST_BREAD.get());
                    output.accept(ModItems.FOOD_STAR.get());
                    output.accept(ModItems.FISHERY_STAR.get());
                    output.accept(ModItems.FARMING_STAR.get());
                    output.accept(ModItems.FORESTRY_STAR.get());
                    output.accept(ModItems.WEALTH_STAR.get());
                    output.accept(ModItems.ANIMAL_HUSBANDRY_STAR.get());
                    output.accept(ModItems.FARMERS_RANCH_STAR.get());
                    output.accept(ModItems.COPPER_CERTIFICATE.get());
                    output.accept(ModItems.CUPRONICKEL_CERTIFICATE.get());
                    output.accept(ModItems.SILVER_CERTIFICATE.get());
                    output.accept(ModItems.GOLD_CERTIFICATE.get());
                    output.accept(ModItems.PLATINUM_CERTIFICATE.get());
                    output.accept(ModItems.OSMIUM_CERTIFICATE.get());
                    output.accept(ModItems.NAQUADAH_CERTIFICATE.get());
                    output.accept(ModItems.NEUTRONIUM_CERTIFICATE.get());
                    output.accept(ModItems.GUMMY_BEAR.get());       //柑橘味小熊软糖
                    output.accept(ModItems.COOK_ZHURUOLING_EGG.get());
                    output.accept(ModItems.ZHURUOLING_EGG.get());
                    output.accept(ModItems.COLLAPSE_DRONE.get());
                    output.accept(ModItems.COLLAPSE_QUEEN.get());
                    output.accept(ModItems.COLLAPSE_PRINCESS.get());
                    output.accept(ModItems.HARSH_WINTER_CREDIT.get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    output.accept(ModBlocks.TREE_COMPOST.get());  //树坑
                    output.accept(ModBlocks.FISHPOND_CORE.get());
                    output.accept(ModBlocks.GREENHOUSE_FURNACE.get());
                    output.accept(ModBlocks.FORESTRY_GREENHOUSE.get());
                    output.accept(ModBlocks.FORESTRY_HYBRIDIZER.get());
                    output.accept(ModBlocks.GREENHOUSE_CASING.get());
                    output.accept(ModBlocks.TAPPER.get());
                    })
                    .build()
    );
 

    // 鱼类标签页
    public static final RegistryObject<CreativeModeTab> FISH_TAB = CREATIVE_TABS.register("crops_fish", () ->
            CreativeModeTab.builder()
                    .title(REGISTRATE.addLang("itemGroup", ListMod.rl("crops_fish"), "Fish"))  // 语言键: itemGroup.listmod.crops
                    .icon(() -> new ItemStack(ModItems.DUNGENESS_CRAB.get()))
                    .displayItems((parameters, output) -> {

                    output.accept(ModItems.EMPEROR_CRAB.get());
                    output.accept(ModItems.JONAH_CRAB.get());
                    output.accept(ModItems.PELAGIC_SWIMMING_CRAB.get());
                    output.accept(ModItems.DUNGENESS_CRAB.get());
                    output.accept(ModItems.SWIMMING_CRAB.get());
                    output.accept(ModItems.HERMIT_CRAB.get());
                    output.accept(ModItems.RIVER_CRAB.get());  //河蟹
                    //output.accept(ModItems.蓝龙虾.get());    //蓝龙虾
                    //output.accept(ModItems.锦绣龙虾.get());  //锦绣龙虾
                    output.accept(ModItems.DEEP_SEA_STAG_SHRIMP.get());  //深海鳌虾
                    output.accept(ModItems.GREASYBACK_SHRIMP.get());    //基围虾
                    output.accept(ModItems.MANTIS_SHRIMP.get());    //濑尿虾
                    output.accept(ModItems.RIVER_SHRIMP.get());
                    //output.accept(ModItems.海螺.get());
                    //output.accept(ModItems.甲鱼.get());
                    output.accept(ModItems.GRASS_CARP.get());      //草鱼
                    output.accept(ModItems.SINIPERCA_CHUATSI.get());   //鳜鱼
                    output.accept(ModItems.LARGE_YELLOW_CROAKER.get());   //大黄鱼
                    output.accept(ModItems.SILVER_POMFRET.get());   //银鲳鱼
                    output.accept(ModItems.TURBOT.get());   //多宝鱼
                    output.accept(ModItems.RED_GROUPER.get());   //红石斑鱼
                    output.accept(ModItems.PARROTFISH.get());   //鹦嘴鱼
                    output.accept(ModItems.CAMOUFLAGE_GROUPER.get());   //苏眉鱼
                    output.accept(ModItems.KOI.get());    //锦鲤
                    output.accept(ModItems.THREADFIN_BREAM.get());         // 鸡鱼
                    output.accept(ModItems.CHECKERBOARD_WRASSE.get());     // 云斑海猪鱼
                    //output.accept(ModItems.MORAY_EEL.get());               // 海鳗
                    output.accept(ModItems.SPANISH_MACKEREL.get());        // 鲅鱼
                    output.accept(ModItems.STRIPED_BASS.get());            // 条纹鲈鱼
                    output.accept(ModItems.BIGHEAD_CARP.get());            // 鳙鱼o
                    output.accept(ModItems.CHINESE_SHAD.get());            // 鲥鱼
                    output.accept(ModItems.PACIFIC_SAURY.get());           // 秋刀鱼
                    output.accept(ModItems.FIRE_RAT_FISH.get());           // 火鼠鱼
                    output.accept(ModItems.ICETHORN_FISH.get());           // 冰棘鱼
                    output.accept(ModItems.SNAKEHEAD_FISH.get());          // 蛇鱼
                    output.accept(ModItems.RED_SNAPPER.get());             // 红鲷鱼
                    output.accept(ModItems.DEVIL_RAY.get());               // 魔鬼鱼
                    output.accept(ModItems.LONGJAW_COILIA.get());          // 长颌鲚
                    output.accept(ModItems.SMALL_WHITEBAIT.get());         // 小银鱼
                    output.accept(ModItems.ROSY_BARB.get());               // 胭脂鱼
                    output.accept(ModItems.ANCHOVY.get());                 // 凤尾鱼
                    output.accept(ModItems.HORSEFACE_FISH.get());          // 马面鱼
                    //output.accept(ModItems.RICE_PADDY_EEL.get());          // 黄鳝
                    output.accept(ModItems.FLUKE.get());               // 鲽鱼
                    output.accept(ModItems.BLACK_AMUR_BREAM.get());        // 乌青鱼
                    output.accept(ModItems.BREAM.get());                   // 鳊鱼
                    output.accept(ModItems.SARDINE.get());                 // 沙丁鱼
                    output.accept(ModItems.SEA_BASS.get());                // 海鲈鱼
                    output.accept(ModItems.RED_SONG_CARP.get());           // 红歌鲤
                    output.accept(ModItems.BARRACUDA.get());               // 巴浪鱼
                    output.accept(ModItems.THREE_KNIFE_FISH.get());        // 三刀鱼
                    output.accept(ModItems.HORSEHEAD_FISH.get());          // 马头鱼
                    output.accept(ModItems.FLOWER_SPOTTED_GROUPER.get());  // 花英斑
                    output.accept(ModItems.DEEP_SEA_SALMON.get());         // 深海鲑鱼
                    output.accept(ModItems.BLUE_FLYING_FISH.get());        // 蓝飞鱼
                    //output.accept(ModItems..get());  //红罗非鱼
                    //output.accept(ModItems..get());  //比目鱼
                    //output.accept(ModItems.FLYING_FISH.get());             // 飞鱼
                    output.accept(ModItems.RAINFISH.get());                // 雨鱼
                    output.accept(ModItems.WHITE_STRIPE_FISH.get());       // 白条鱼
                    //output.accept(ModItems.RED_STINGRAY.get());            // 赤魟
                    output.accept(ModItems.NEEDLEFISH.get());              // 针鱼
                    output.accept(ModItems.SOLE_FISH.get());               // 龙利鱼
                    output.accept(ModItems.CHUB_MACKEREL.get());           // 青花鱼
                    output.accept(ModItems.CHINESE_PERCH.get());           // 桂鱼
                    //output.accept(ModItems.HAIRTAIL.get());                // 带鱼
                    //output.accept(ModItems.CHUM_SALMON.get());             // 大马哈鱼
                    //output.accept(ModItems.BLACKFIN_SHARK.get());          // 乌翅真鲨
                    //output.accept(ModItems.BLOOD_RED_DRAGONFISH.get());    // 血红龙
                    //output.accept(ModItems.RED_SEA_GOLDEN_BUTTERFLY.get()); // 红海黄金蝶
                    //output.accept(ModItems.PURPLE_SEA_JELLYFISH.get());    // 紫海刺水母
                    //output.accept(ModItems.CROWN_DOGHEAD.get());           // 皇冠狗头
                    //output.accept(ModItems.FLOWER_HIGHHEAD_DRAGONEYE.get());// 花高头龙睛
                    //output.accept(ModItems.STRIPED_SEAHORSE.get());        // 线纹海马
                    //output.accept(ModItems.YELLOWTAIL_BLUE_DEMON.get());   // 黄尾蓝魔
                    //output.accept(ModItems.PERUVIAN_FAIRYFISH.get());      // 秘鲁神仙
                    //output.accept(ModItems.THREE_SPOTTED_ROCKETFISH.get());// 三间火箭
                    //output.accept(ModItems.RED_SWORDFISH.get());           // 红剑
                    //output.accept(ModItems.LUZON_SPINY_STARFISH.get());    // 吕宋棘海星
                    //output.accept(ModItems.RED_BELLIED_PIRANHA.get());     // 红腹食人鲳
                    //output.accept(ModItems.GRASS_CARP_KING.get());            //草鱼王
                    //output.accept(ModItems.RIVER_SHRIMP_KING.get());          //对虾王
                    //output.accept(ModItems.RIVER_CRAB_KING.get());            //河蟹王
                    //output.accept(ModItems.CARP_KING.get());                  //鲤鱼王
                    //output.accept(ModItems.PUFFERFISH_KING.get());            //河豚王
                    //output.accept(ModItems.CATFISH_KING.get());               //鲶鱼王
                    //output.accept(ModItems.MANDARIN_FISH_KING.get());         //鳜鱼王（桂花鱼）
                    //output.accept(ModItems.RED_TILAPIA_KING.get());           //红罗非鱼王
                    //output.accept(ModItems.LARGE_YELLOW_CROAKER_KING.get());  //大黄鱼王
                    //output.accept(ModItems.SILVER_POMFRET_KING.get());        //银鲳鱼王
                    //output.accept(ModItems.HAIRTAIL_KING.get());              //带鱼王
                    //output.accept(ModItems.TURBOT_KING.get());                //多宝鱼王
                    //output.accept(ModItems.RED_GROUPER_KING.get());           //红石斑鱼王
                    //output.accept(ModItems.SALMON_KING.get());                //鲑鱼王
                    //output.accept(ModItems.PARROT_FISH_KING.get());           //鹦嘴鱼王
                    //output.accept(ModItems.LOBSTER_KING.get());               //龙虾王
                    //output.accept(ModItems.NAPOLEON_FISH_KING.get());         //苏眉鱼王（拿破仑鱼）
                    //output.accept(ModItems.THREADFIN_BREAM_KING.get());       //鸡鱼王（多指鸡鱼/鲷类，最常见为鲷科 Threadfin Bream）
                    //output.accept(ModItems.SQUID_KING.get());                 //鱿鱼王
                    //output.accept(ModItems.CHECKERBOARD_WRASSE_KING.get());   //云斑海猪鱼王
                    //output.accept(ModItems.MORAY_EEL_KING.get());             //海鳗王
                    //output.accept(ModItems.SOFTSHELL_TURTLE_KING.get());      //甲鱼王
                    //output.accept(ModItems.SWIMMING_CRAB_KING.get());         //梭子蟹王
                    //output.accept(ModItems.SPANISH_MACKEREL_KING.get());      //鲅鱼王
                    //output.accept(ModItems.STRIPED_BASS_KING.get());          //条纹鲈鱼王
                    //output.accept(ModItems.RAINBOW_TROUT_KING.get());         //虹鳟鱼王
                    //output.accept(ModItems.BLUEGILL_KING.get());              //蓝鳃太阳鱼王
                    //output.accept(ModItems.BIGHEAD_CARP_KING.get());          //鳙鱼王
                    //output.accept(ModItems.PACIFIC_SAURY_KING.get());         //秋刀鱼王
                    //output.accept(ModItems.RAINFISHI_KING.get());             //雨鱼王
                    //output.accept(ModItems.FIRE_MOUSEFISH_KING.get());        //火鼠鱼王（幻想生物，保留直译）
                    //output.accept(ModItems.ICE_THORN_FISH_KING.get());        //冰棘鱼王（幻想生物直译）
                    //output.accept(ModItems.SNAKEHEAD_KING.get());             //蛇鱼王（黑鱼 Snakehead）
                    //output.accept(ModItems.SOLE_FISH_KING.get());             //龙利鱼王（多指鲽目 Sole 或 Flounder，这里取 Sole）
                    //output.accept(ModItems.RED_SNAPPER_KING.get());           //红鲷鱼王
                    //output.accept(ModItems.TOPMOUTH_GUDGEON_KING.get());      //白条鱼王（常见“白条鱼”= Topmouth gudgeon）
                    //output.accept(ModItems.TUNA_KING.get());                  //金枪鱼王
                    //output.accept(ModItems.CLOWNFISH_KING.get());             //小丑鱼王
                    //output.accept(ModItems.MANTA_RAY_KING.get());             //魔鬼鱼王（Manta Ray）
                    //output.accept(ModItems.COCKLE.get());                 //鸟蛤（就是鸟贝） cockle      //蟹 虾 贝 软体 鱼
                    //output.accept(ModItems.CLAM.get());                   // 蚌（游戏中为 Clam）
                    //output.accept(ModItems.OYSTER.get());                 // 牡蛎
                    //output.accept(ModItems.NAUTILUS_SHELL.get());         // 玉蜀螺（官方：Nautilus Shell）
                    //output.accept(ModItems.CRIMSONFISH.get());            // 绯红鱼（传奇鱼之一：Crimsonfish）
                    //output.accept(ModItems.LEGEND.get());                 // 传说之鱼（Legend）
                    //output.accept(ModItems.GLACIERFISH.get());            // 冰川鱼（Glacierfish）
                    //output.accept(ModItems.MUTANT_CARP.get());            // 变种鲤鱼（Mutant Carp）
                    //output.accept(ModItems.SON_OF_CRIMSONFISH.get());     // 绯红鱼之子（Son of Crimsonfish）
                    //output.accept(ModItems.Ms_ANGELFISH.get());           // 雌𩽾𩾌鱼
                    //output.accept(ModItems.LEGEND_II.get());              // 传说之鱼二代（Legend II）
                    //output.accept(ModItems.GLACIERFISH_JR.get());         // 小冰川鱼（Glacierfish Jr.）
                    //output.accept(ModItems.RADIOACTIVE_CARP.get());       // 放射性鲤鱼（Radioactive Carp）
                    //output.accept(ModItems.MIDNIGHT_SQUID.get());         // 午夜鱿鱼（Midnight Squid）
                    //output.accept(ModItems.GHOSTFISH.get());              // 幽灵鱼（Ghostfish）
                    //output.accept(ModItems.BLOBFISH.get());               // 水滴鱼（Blobfish）
                    //output.accept(ModItems.GOBY.get());                   // 虾虎鱼（Goby，游戏未收录，为通用译名）
                    //output.accept(ModItems.BLUE_DISC_CUSKEEL.get());      // 蓝饼铁鱼（依据形态翻译）
                    //output.accept(ModItems.LIONFISH.get());               // 狮子鱼（Lionfish）
                    //output.accept(ModItems.STINGRAY.get());               // 黄貂鱼（Stingray）
                    //output.accept(ModItems.SLIMEJACK.get());              // 史莱姆鱼（Slimejack）
                    //output.accept(ModItems.VOID_SALMON.get());            // 虚空鲑鱼（Void Salmon）
                    //output.accept(ModItems.WOODSKIP.get());               // 木跃鱼（Woodskip）
                    //output.accept(ModItems.SNAILEY_ONE_LINE_FISH.get());  // 蛇齿单线鱼（无官方名 → 合理科学译名）
                    //output.accept(ModItems.HERRING.get());                // 西鲱（Herring）
                    //output.accept(ModItems.CHUB.get());                   // 大头鱼（Chub）
                    //output.accept(ModItems.TIGER_TROUT.get());            // 虎纹鳟鱼（Tiger Trout）
                    //output.accept(ModItems.STURGEON.get());               // 鲟鱼（Sturgeon）
                    //output.accept(ModItems.MIDNIGHT_CARP.get());          // 午夜鲤鱼（Midnight Carp）
                    //output.accept(ModItems.SAND_FISH.get());              // 沙鱼（Sandfish）
                    //output.accept(ModItems.LAVA_EEL.get());               // 岩浆鳗鱼（Lava Eel）
                    //output.accept(ModItems.ICE_PIP.get());                // 冰柱鱼（Ice Pip）
                    //output.accept(ModItems.STONEFISH.get());              // 石鱼（Stonefish）
                    //output.accept(ModItems.SPIRIT_EEL.get());             // 鬼鱼（Spirit Eel）
                    //output.accept(ModItems.SEA_CUCUMBER.get());           // 大海参（Sea Cucumber，注意区分尺寸时用 Super Cucumber）
                    //output.accept(ModItems.SUPER_CUCUMBER.get());         // 海参（Super Cucumber）
                    //output.accept(ModItems.RED_MULLET.get());             // 红鲻鱼（Red Mullet）
                    //output.accept(ModItems.SUNFISH.get());                // 太阳鱼（Sunfish）
                    //output.accept(ModItems.CoconutCrab.get());  //椰子蟹  现实
                    //output.accept(ModItems.SpiderCrab.get());  //蜘蛛蟹
                    //output.accept(ModItems.SnowCrab.get());  //雪蟹
                    //output.accept(ModItems.CrystalCrab.get());  //水晶蟹（商品名称一般是澳洲雪蟹，事实上和雪蟹没有半毛钱关系）
                    //output.accept(ModItems.RedKingCrab.get());  //松叶蟹（注意：松叶蟹对应 red king crab / japanese king crab）
                    //output.accept(ModItems.TigerCrab.get());  //老虎蟹
                    //output.accept(ModItems.GiantMudCrab.get());  //甘氏巨鳌蟹
                    //output.accept(ModItems.MyanmarMudCrab.get());  //缅甸青蟹
                    //output.accept(ModItems.ChineseHorseshoeCrab.get());  //中华鲎
                    //output.accept(ModItems.GiantMarineLobster.get());  //巨象真龙虾
                    //output.accept(ModItems.EuropeanLobster.get());  //欧洲龙虾  这玩意我吃过，我记得是蓝色的
                    //output.accept(ModItems.SouthAustraliaRockLobster.get());  //南澳岩龙虾
                    output.accept(ModItems.CRAYFISH.get());  //小龙虾
                    output.accept(ModItems.TASMANIAN_GIANT_FRESHWATER_CRAYFISH.get());  //塔斯马尼亚巨型淡水龙虾
                    //output.accept(ModItems.FitzroyRiverYabby.get());  //费氏巨鳌虾
                    //output.accept(ModItems.TriangleYabby.get());  //三角巨鳌虾
                    output.accept(ModItems.EUASTACUS_ARMATUS.get());  //墨累河白刺鳌虾
                    //output.accept(ModItems.AUSTRALIA_CHERAX_QUADRICARINATUS.get());  //澳洲红鳌虾
                    output.accept(ModItems.MARRON_CRAYFISH.get());  //马龙鳌虾
                    output.accept(ModItems.CAMBAROIDES_DAURICUS.get());  //中华长白山鳌虾 其实是蝲蛄
                    //output.accept(ModItems.SlipperLobster.get());  //琵琶虾
                    //output.accept(ModItems.MantisShrimp.get());  //螳螂虾
                    //output.accept(ModItems.RhinocerosShrimp.get());  //非洲犀牛虾
                    //output.accept(ModItems.GiantRiverPrawn.get());  //罗氏沼虾
                    //output.accept(ModItems.BotaniShrimp.get());  //牡丹虾
                    //output.accept(ModItems.GrapeShrimp.get());  //葡萄虾
                    //output.accept(ModItems.GiantIsopod.get());  //大王具足虫
                    //output.accept(ModItems.NewZealandBlackFootAbalone.get());  //新西兰黑金鲍
                    //output.accept(ModItems.AustralianGoldenAbalone.get());  //澳洲黄金鲍
                    //output.accept(ModItems.AlaskaRedAbalone.get());  //阿拉斯加红鲍
                    //output.accept(ModItems.AustralianBlacklipAbalone.get());  //澳洲黑唇鲍
                    //output.accept(ModItems.AustralianGreenlipAbalone.get());  //澳洲青唇鲍
                    //output.accept(ModItems.GooseneckBarnacle.get());  //鹅颈藤壶
                    //output.accept(ModItems.VolcanicBarnacle.get());  //火山口藤壶
                    //output.accept(ModItems.Geoduck.get());  //象拔蚌
                    //output.accept(ModItems.CoconutSnail.get());  //椰子螺
                    //output.accept(ModItems.AfricanTurboSnail.get());  //非洲涡螺
                    //output.accept(ModItems.CatseyeSnail.get());  //猫眼螺
                    //output.accept(ModItems.FlowerSnail.get());  //花螺
                    //output.accept(ModItems.QueenConch.get());  //响螺
                    //output.accept(ModItems.FragrantConch.get());  //香螺
                    //output.accept(ModItems.MudSnail.get());  //泥螺
                    //output.accept(ModItems.FieldSnail.get());  //田螺
                    //output.accept(ModItems.NailSnail.get());  //钉螺
                    //output.accept(ModItems.RiverSnail.get());  //螺狮
                    //output.accept(ModItems.JadeSnail.get());  //翡翠螺
                    //output.accept(ModItems.FreshwaterClam.get());  //河蚬（淡水蛤蜊）
                    //output.accept(ModItems.ArcticSurfClam.get());  //北极贝
                    //output.accept(ModItems.Scallop.get());  //扇贝
                    //output.accept(ModItems.Clam.get());  //蛤蜊
                    //output.accept(ModItems.RazorClam.get());  //竹蛏
                    //output.accept(ModItems.BloodCockle.get());  //血蛤
                    //output.accept(ModItems.ChineseMahuaClam.get());  //中华马珂蛤
                    //output.accept(ModItems.WaterPineClam.get());  //水松贝
                    //output.accept(ModItems.AsariClam.get());  //文蛤
                    //output.accept(ModItems.Akabai.get());  //赤贝
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems.辽参.get());
                    //output.accept(ModItems.日本海参.get());
                    //output.accept(ModItems.太平洋刺参.get());
                    //output.accept(ModItems.阿拉斯加红海参.get());
                    //output.accept(ModItems.紫海胆.get());
                    //output.accept(ModItems.绿海胆.get());
                    //output.accept(ModItems.红海胆.get());
                    //output.accept(ModItems.月亮鱼.get());
                    //output.accept(ModItems.龙趸石斑鱼.get());
                    //output.accept(ModItems.马林鱼（剑鱼）.get());
                    //output.accept(ModItems.鲣鱼.get());
                    //output.accept(ModItems.泥鳅.get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());                  //长江鲥鱼 蓝旗金枪鱼 黄旗金枪鱼 皇带鱼 欧洲鳗鱼 帝王鲑

                     })
                    .build()
    );



                    //output.accept(ModItems.蓝龙虾.get());
                    //output.accept(ModItems.锦绣龙虾.get());
                    //output.accept(ModItems.深海鳌虾.get());
                    //output.accept(ModItems.基围虾.get());
                    //output.accept(ModItems.濑尿虾.get());
                    //output.accept(ModItems.河虾.get());
                    //output.accept(ModItems.海螺片.get());
                    //output.accept(ModItems.生草鱼片.get());
                    //output.accept(ModItems.生蕨鱼片.get());
                    //output.accept(ModItems.生大黄鱼片.get());
                    //output.accept(ModItems.生银鲳鱼片.get());
                    //output.accept(ModItems.生多宝鱼片.get());
                    //output.accept(ModItems.生红石斑鱼片.get());
                    //output.accept(ModItems.生鹦嘴鱼片.get());
                    //output.accept(ModItems.生苏眉鱼片.get());
                    //output.accept(ModItems.生锦鲤片.get());
                    //output.accept(ModItems.生鸡鱼片.get());
                    //output.accept(ModItems.生云斑海猪鱼片.get());
                    //output.accept(ModItems.生海鳗片.get());
                    //output.accept(ModItems.生鲅鱼片.get());
                    //output.accept(ModItems.生条纹鲈鱼片.get());
                    //output.accept(ModItems.生鳙鱼片.get());
                    //output.accept(ModItems.生鲥鱼片.get());
                    //output.accept(ModItems.生秋刀鱼片.get());
                    //output.accept(ModItems.生火鼠鱼片.get());
                    //output.accept(ModItems.生冰棘鱼片.get());
                    //output.accept(ModItems.生蛇鱼片.get());
                    //output.accept(ModItems.生红鲷鱼片.get());
                    //output.accept(ModItems.生魔鬼鱼片.get());
                    //output.accept(ModItems.生长颌鲚片.get());
                    //output.accept(ModItems.生小银鱼片.get());
                    //output.accept(ModItems.生胭脂鱼片.get());
                    //output.accept(ModItems.生凤尾鱼片.get());
                    //output.accept(ModItems.生马面鱼片.get());
                    //output.accept(ModItems.生黄鳝片.get());
                    //output.accept(ModItems.生鲽鱼片.get());
                    //output.accept(ModItems.生乌青鱼片.get());
                    //output.accept(ModItems.生鳊鱼片.get());
                    //output.accept(ModItems.生沙丁鱼片.get());
                    //output.accept(ModItems.生海鲈鱼片.get());
                    //output.accept(ModItems.生红歌鲤片.get());
                    //output.accept(ModItems.生巴浪鱼片.get());
                    //output.accept(ModItems.生三刀鱼片.get());
                    //output.accept(ModItems.生马头鱼片.get());
                    //output.accept(ModItems.生花英斑片.get());
                    //output.accept(ModItems.生深海鲑鱼片.get());
                    //output.accept(ModItems.生蓝飞鱼片.get());
                    //output.accept(ModItems.生飞鱼片.get());
                    //output.accept(ModItems.生雨鱼片.get());
                    //output.accept(ModItems.生白条鱼片.get());
                    //output.accept(ModItems.生赤魟片.get());
                    //output.accept(ModItems.生针鱼片.get());
                    //output.accept(ModItems.生龙利鱼片.get());
                    //output.accept(ModItems.生青花鱼片.get());
                    //output.accept(ModItems.生桂鱼片.get());
                    //output.accept(ModItems.生带鱼片.get());
                    //output.accept(ModItems.生大马哈鱼片.get())
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());




     //卵
    public static final RegistryObject<CreativeModeTab> EGG_TAB = CREATIVE_TABS.register("list_egg", () ->
            CreativeModeTab.builder()
                    .title(REGISTRATE.addLang("itemGroup", ListMod.rl("list_egg"), "Egg"))  // 语言键: itemGroup.listmod.list
                    .icon(() -> new ItemStack(ModItems.EMPEROR_CRAB_EGG.get()))
                    .displayItems((parameters, output) -> {
                         output.accept(ModItems.EMPEROR_CRAB_EGG.get());
                    output.accept(ModItems.JONAH_CRAB_EGG.get());
                    output.accept(ModItems.PELAGIC_SWIMMING_CRAB_EGG.get());
                    output.accept(ModItems.DUNGENESS_CRAB_EGG.get());
                    output.accept(ModItems.SWIMMING_CRAB_EGG.get());
                    output.accept(ModItems.HERMIT_CRAB_EGG.get());
                    output.accept(ModItems.RIVER_CRAB_EGG.get());
                    output.accept(ModItems.BLUE_LOBSTER_EGG.get());
                    output.accept(ModItems.MARBLED_LOBSTER_EGG.get());
                    output.accept(ModItems.DEEP_SEA_STAG_SHRIMP_EGG.get());
                    output.accept(ModItems.VANNAMEI_SHRIMP_EGG.get());
                    output.accept(ModItems.MANTIS_SHRIMP_EGG.get());
                    output.accept(ModItems.RIVER_SHRIMP_EGG.get());
                    //output.accept(ModItems.海螺卵.get());
                    //output.accept(ModItems.甲鱼卵.get());
                    output.accept(ModItems.GRASS_CARO_EGG.get());
                    output.accept(ModItems.MANDARIN_FISH_EGG.get());
                    output.accept(ModItems.LARGE_YELLOW_CROAKER_EGG.get());
                    output.accept(ModItems.SILVER_POMFRET_EGG.get());
                    output.accept(ModItems.TURBOT_EGG.get());
                    output.accept(ModItems.RED_GROUPER_EGG.get());
                    output.accept(ModItems.PARROT_FISH_EGG.get());
                    output.accept(ModItems.CAMOUFLAGE_GROUPER_EGG.get());
                    output.accept(ModItems.KOI_EGG.get());   //锦鲤卵
                    output.accept(ModItems.THREADFIN_BREAM_EGG.get());
                    output.accept(ModItems.CHECKERBOARD_WRASSE_EGG.get());
                    output.accept(ModItems.MORAY_EEL_EGG.get());
                    output.accept(ModItems.SPANISH_MACKEREL_EGG.get());
                    output.accept(ModItems.STRIPED_BASS_EGG.get());
                    output.accept(ModItems.BIGHEAD_CARP_EGG.get());
                    output.accept(ModItems.CHINESE_SHAD_EGG.get());
                    output.accept(ModItems.PACIFIC_SAURY_EGG.get());
                    output.accept(ModItems.FIRE_RAT_FISH_EGG.get());
                    output.accept(ModItems.ICETHORN_FISH_EGG.get());
                    output.accept(ModItems.SNAKEHEAD_FISH_EGG.get());          // 蛇鱼卵
                    output.accept(ModItems.RED_SNAPPER_EGG.get());             // 红鲷鱼卵
                    output.accept(ModItems.DEVIL_RAY_EGG.get());               // 魔鬼鱼卵
                    output.accept(ModItems.LONGJAW_COILIA_EGG.get());          // 长颌鲚卵
                    output.accept(ModItems.SMALL_WHITEBAIT_EGG.get());         // 小银鱼卵
                    output.accept(ModItems.ROSY_BARB_EGG.get());               // 胭脂鱼卵
                    output.accept(ModItems.ANCHOVY_EGG.get());                 // 凤尾鱼卵
                    output.accept(ModItems.HORSEFACE_FISH_EGG.get());          // 马面鱼卵
                    output.accept(ModItems.RICE_PADDY_EEL_EGG.get());          // 黄鳝卵
                    output.accept(ModItems.FLUKE_EGG.get());                   // 鲽鱼卵
                    output.accept(ModItems.BLACK_AMUR_BREAM_EGG.get());        // 乌青鱼卵
                    output.accept(ModItems.BREAM_EGG.get());                   // 鳊鱼卵
                    output.accept(ModItems.SARDINE_EGG.get());                 // 沙丁鱼卵
                    output.accept(ModItems.SEA_BASS_EGG.get());                // 海鲈鱼卵
                    output.accept(ModItems.RED_SONG_CARP_EGG.get());           // 红歌鲤卵
                    output.accept(ModItems.BARRACUDA_EGG.get());               // 巴浪鱼卵
                    output.accept(ModItems.THREE_KNIFE_FISH_EGG.get());        // 三刀鱼卵
                    output.accept(ModItems.HORSEHEAD_FISH_EGG.get());          // 马头鱼卵
                    output.accept(ModItems.FLOWER_SPOTTED_GROUPER_EGG.get());  // 花英斑卵
                    output.accept(ModItems.DEEP_SEA_SALMON_EGG.get());         // 深海鲑鱼卵
                    output.accept(ModItems.BLUE_FLYING_FISH_EGG.get());        // 蓝飞鱼卵
                    output.accept(ModItems.FLYING_FISH_EGG.get());             // 飞鱼卵
                    output.accept(ModItems.RAINFISH_EGG.get());                // 雨鱼卵
                    output.accept(ModItems.WHITE_STRIPE_FISH_EGG.get());       // 白条鱼卵
                    output.accept(ModItems.RED_STINGRAY_EGG.get());            // 赤魟卵
                    output.accept(ModItems.NEEDLEFISH_EGG.get());              // 针鱼卵
                    output.accept(ModItems.SOLE_FISH_EGG.get());               // 龙利鱼卵
                    output.accept(ModItems.CHUB_MACKEREL_EGG.get());           // 青花鱼卵
                    output.accept(ModItems.CHINESE_PERCH_EGG.get());           // 桂鱼卵
                    output.accept(ModItems.HAIRTAIL_EGG.get());                // 带鱼卵
                    output.accept(ModItems.CHUM_SALMON_EGG.get());             // 大马哈鱼卵
                    output.accept(ModItems.BLACKFIN_SHARK_EGG.get());          // 乌翅真鲨卵
                    output.accept(ModItems.BLOOD_RED_DRAGONFISH_EGG.get());    // 血红龙卵
                    output.accept(ModItems.RED_SEA_GOLDEN_BUTTERFLY_EGG.get()); // 红海黄金蝶卵
                    output.accept(ModItems.PURPLE_SEA_JELLYFISH_EGG.get());    // 紫海刺水母卵
                    output.accept(ModItems.CROWN_DOGHEAD_EGG.get());           // 皇冠狗头卵
                    output.accept(ModItems.FLOWER_HIGHHEAD_DRAGONEYE_EGG.get());// 花高头龙睛卵
                    output.accept(ModItems.STRIPED_SEAHORSE_EGG.get());        // 线纹海马卵
                    output.accept(ModItems.YELLOWTAIL_BLUE_DEMON_EGG.get());   // 黄尾蓝魔卵
                    output.accept(ModItems.PERUVIAN_FAIRYFISH_EGG.get());      // 秘鲁神仙卵
                    output.accept(ModItems.THREE_SPOTTED_ROCKETFISH_EGG.get());// 三间火箭卵
                    output.accept(ModItems.RED_SWORDFISH_EGG.get());           // 红剑卵
                    output.accept(ModItems.LUZON_SPINY_STARFISH_EGG.get());    // 吕宋棘海星卵
                    output.accept(ModItems.RED_BELLIED_PIRANHA_EGG.get());     // 红腹食人鲳卵
                    output.accept(ModItems.LAVA_PUFFERFISH_EGG.get());  //熔岩河豚卵
                    output.accept(ModItems.OBSIDIANFISH_EGG.get());
                    output.accept(ModItems.SEARING_COD_EGG.get());
                    output.accept(ModItems.BONEFISH_EGG.get());
                    output.accept(ModItems.WITHER_BONEFISH_EGG.get());
                    output.accept(ModItems.BLAZEFISH_EGG.get());
                    output.accept(ModItems.MAGMACUBEFISH_EGG.get());
                    output.accept(ModItems.GLOWDINE_EGG.get());
                    output.accept(ModItems.SOULSUCKER_EGG.get());
                    output.accept(ModItems.FORTRESS_GROUPER_EGG.get());
                    output.accept(ModItems.EYEBALL_FISH_EGG.get());  //眼球鱼卵
                    output.accept(ModItems.FLAME_SQUAT_LOBSTER_EGG.get());    // 烈焰铠甲虾卵
                    output.accept(ModItems.OBSIDIAN_SWORD_FISH_EGG.get());    // 黑曜石剑鱼卵
                    output.accept(ModItems.STEAM_FLYING_FISH_EGG.get());      // 蒸汽飞鱼卵
                    output.accept(ModItems.AGNI_FISH_EGG.get());              // 火神鱼卵
                    output.accept(ModItems.AROWANA_FISH_EGG.get());           // 龙鱼卵
                    output.accept(ModItems.QUARTZ_FISH_EGG.get());            // 石英鱼卵
                    output.accept(ModItems.SCALY_FOOT_SNAIL_EGG.get());       // 鳞足蜗牛卵
                    output.accept(ModItems.YETI_CRAB_EGG.get());              // 雪人蟹卵
                    output.accept(ModItems.LAVA_LAMPREY_EGG.get());           // 熔岩七鳃鳗卵
                    output.accept(ModItems.EYELASH_EGG.get());                    // 睫鱼卵
                    output.accept(ModItems.SPINDLEFISH_EGG.get());                // 纺锤鲉卵
                    output.accept(ModItems.SNOWFLAKE_EGG.get());                  // 雪尾鱼卵
                    output.accept(ModItems.AERO_MONO_EGG.get());                  // 气旋鲳鱼卵
                    output.accept(ModItems.PICKLEFISH_EGG.get());                 // 泡菜鱼卵
                    output.accept(ModItems.AMBER_GOBY_EGG.get());                 // 琥珀虾虎鱼卵
                    output.accept(ModItems.BEAKED_HERRING_EGG.get());             // 喙鲱鱼卵
                    output.accept(ModItems.BLIND_SAILFIN_EGG.get());              // 盲帆鳍鱼卵
                    output.accept(ModItems.CIRCUS_FISH_EGG.get());                // 马戏鱼卵
                    output.accept(ModItems.COPPERFLAME_ANTHIAS_EGG.get());        // 铜焰花鮨卵
                    output.accept(ModItems.DEMON_HERRING_EGG.get());              // 恶魔鲱鱼卵
                    output.accept(ModItems.DROOPING_GOURAMI_EGG.get());           // 垂丝攀鲈卵
                    output.accept(ModItems.DUALITY_DAMSELFISH_EGG.get());         // 阴阳雀鲷卵
                    output.accept(ModItems.FORKFISH_EGG.get());                   // 叉鱼卵
                    output.accept(ModItems.HATCHETFISH_EGG.get());                // 斧鱼卵
                    output.accept(ModItems.SNEEP_SNORP_EGG.get());                // 长鼻鱼卵
                    output.accept(ModItems.TRIPLE_TWIRL_PLECO_EGG.get());         // 三旋甲鲶卵
                    output.accept(ModItems.SAILOR_BARB_EGG.get());                // 水手钩鱼卵
                    output.accept(ModItems.BARK_ANGELFISH_EGG.get());             // 树皮神仙鱼卵
                    output.accept(ModItems.BOULTI_EGG.get());            // 尼罗罗非鱼卵
                    output.accept(ModItems.CAPITAINE_EGG.get());         // 尼罗河尖吻鲈卵
                    output.accept(ModItems.SYNODONTIS_EGG.get());        // 反游猫鱼卵
                    output.accept(ModItems.SMALLMOUTH_BASS_EGG.get());   // 小口黑鲈卵
                    output.accept(ModItems.BROWN_TROUT_EGG.get());       // 褐鳟鱼卵
                    output.accept(ModItems.BLUEGILL_EGG.get());          // 蓝鳃太阳鱼卵
                    output.accept(ModItems.BAYAD_EGG.get());             // 鲿鱼卵
                    output.accept(ModItems.RAINBOW_TROUT_EGG.get());     // 虹鳟鱼卵
                    output.accept(ModItems.POLLOCK_EGG.get());           // 绿青鳕卵
                    output.accept(ModItems.JELLYFISH_EGG.get());             // 水母卵
                    output.accept(ModItems.CARP_EGG.get());          // 鲤鱼卵
                    output.accept(ModItems.CATFISH_EGG.get());       // 鲶鱼卵
                    output.accept(ModItems.GAR_EGG.get());           // 雀鳝卵
                    output.accept(ModItems.MUSKELLUNGE_EGG.get());   // 北美狗鱼卵
                    output.accept(ModItems.MINNOW_EGG.get());        // 鲦鱼卵
                    output.accept(ModItems.PERCH_EGG.get());         // 河鲈鱼卵
                    output.accept(ModItems.ARAPAIMA_EGG.get());      // 巨骨舌鱼卵
                    output.accept(ModItems.PIRANHA_EGG.get());       // 水虎鱼卵
                    output.accept(ModItems.TAMBAQUI_EGG.get());      // 大盖巨脂鲤卵
                    output.accept(ModItems.BROWN_SHROOMA_EGG.get());  // 褐菇鱼卵
                    output.accept(ModItems.RED_SHROOMA_EGG.get());    // 红菇鱼卵
                    output.accept(ModItems.TUNA_EGG.get());           // 金枪鱼卵
                    output.accept(ModItems.RED_DOT_EGG.get());    // 赤点石斑鱼卵
                    output.accept(ModItems.GOLDFISH_EGG.get());       // 金鱼卵
                    output.accept(ModItems.BLACKFISH_EGG.get());          // 黑鱼卵
                    output.accept(ModItems.ATLANTIC_COD_EGG.get());       // 大西洋鳕鱼卵
                    output.accept(ModItems.ATLANTIC_HERRING_EGG.get());   // 大西洋鲱鱼卵
                    output.accept(ModItems.PACIFIC_HALIBUT_EGG.get());    // 太平洋比目鱼卵
                    output.accept(ModItems.ATLANTIC_HALIBUT_EGG.get());   // 大西洋比目鱼卵
                    output.accept(ModItems.PINK_SALMON_EGG.get());        // 粉鲑
                    output.accept(ModItems.TREVALLY_EGG.get());          // 鲹鱼卵
                    output.accept(ModItems.AROWANA_EGG.get());           // 龙鱼卵
                    output.accept(ModItems.SUNFISH_EGG.get());           // 翻车鱼卵
                    output.accept(ModItems.TRIGGERFISH_EGG.get());       // 鳞鲀卵
                    output.accept(ModItems.FOOTBALL_FISH_EGG.get());     // 鮟鱇鱼卵
                    output.accept(ModItems.SPADEFISH_EGG.get());         // 白鲳鱼卵
                    output.accept(ModItems.KING_CRAB_EGG.get());         // 帝王蟹卵
                    output.accept(ModItems.LANTERNFISH_EGG.get());       // 灯笼鱼卵
                    output.accept(ModItems.TRIPODFISH_EGG.get());        // 三角鱼卵
                    output.accept(ModItems.TRILOCARIS_EGG.get());        // 三叶虾卵
                    output.accept(ModItems.RADGILL_EGG.get());           // 腮辐鱼卵
                    output.accept(ModItems.SEA_PIG_EGG.get());               // 海猪卵
                    output.accept(ModItems.MUSSEL_EGG.get());                // 贻贝卵
                    output.accept(ModItems.SWEETISH_FISH_PINK_EGG.get());    // 粉色软糖鱼卵
                    output.accept(ModItems.SWEETISH_FISH_RED_EGG.get());     // 红色软糖鱼卵
                    output.accept(ModItems.SWEETISH_FISH_BLUE_EGG.get());    // 蓝色软糖鱼卵
                    output.accept(ModItems.SWEETISH_FISH_GREEN_EGG.get());   // 绿色软糖鱼卵
                    output.accept(ModItems.SWEETISH_FISH_YELLOW_EGG.get());  // 黄色软糖鱼卵
                    output.accept(ModItems.CLAWSTER_EGG.get());              // 龙虾卵
                    output.accept(ModItems.CLAM_EGG.get());                  // 蛤蜊卵
                    output.accept(ModItems.PLATINUM_BASS_EGG.get());    // 白金鲈鱼卵
                    output.accept(ModItems.URCHIN_EGG.get());           // 海胆卵
                    output.accept(ModItems.SEA_CLAM_EGG.get());         // 海潮蛤卵
                    output.accept(ModItems.TIGER_PRAWN_EGG.get());      // 黑虎虾卵
                    output.accept(ModItems.SHRIMP_EGG.get());           // 虾卵
                    output.accept(ModItems.LAMPREY_EGG.get());          // 八目鳗卵
                    output.accept(ModItems.ABALONE_EGG.get());          // 鲍鱼卵
                    output.accept(ModItems.GUARDIAN_EGG.get());         // 守卫者卵
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    //output.accept(ModItems..get());
                    output.accept(ModItems.CRAYFISH_EGG.get());     // 小龙虾卵
                    output.accept(ModItems.TASMANIAN_GIANT_FRESHWATER_EGG.get());     // 塔斯马尼亚巨型淡水龙虾卵
                    output.accept(ModItems.EUASTACUS_ARMATUS_EGG.get());     //  墨累河白刺鳌虾卵
                    output.accept(ModItems.MARRON_CRAYFISH_EGG.get());     // 马龙鳌虾卵
                    output.accept(ModItems.CAMBAROIDES_DAURICUS_EGG.get());     // 东北鳌虾/蝲蛄卵
                    //output.accept(ModItems..get());     // 卵
                    //output.accept(ModItems..get());     // 卵
                    //output.accept(ModItems..get());     // 卵
                    //output.accept(ModItems..get());     // 卵
                    //output.accept(ModItems..get());     // 卵
                    //output.accept(ModItems..get());     // 卵
                    //output.accept(ModItems..get());     // 卵
                    //output.accept(ModItems..get());     // 卵
                    //output.accept(ModItems..get());     // 卵
                    //output.accept(ModItems..get());     // 卵
                    //output.accept(ModItems..get());     // 卵
                    //output.accept(ModItems..get());     // 卵
                    //output.accept(ModItems..get());     // 卵
                    //output.accept(ModItems..get());     // 卵
                    //output.accept(ModItems..get());     // 卵
                    //output.accept(ModItems..get());     // 卵

                    })
                    .build()
    );

    // 食品与加工品
    public static final RegistryObject<CreativeModeTab> FOOD_AND_PRODUCT_TAB = CREATIVE_TABS.register("food_and_product", () ->
                    CreativeModeTab.builder()
                            .title(REGISTRATE.addLang("itemGroup", ListMod.rl("food_and_product"), "Food and Product"))
                            .icon(() -> new ItemStack(ModItems.RED_CABBAGE_LEAF.get()))
            .displayItems((parameters, output) -> {
                output.accept(ModItems.COOK_GOOSE_EGG.get());       //煎鹅蛋
                output.accept(ModItems.COOK_DUCK_EGG.get());       //煎鸭蛋
                output.accept(ModItems.COOK_TURKEY_EGG.get());       //煎火鸡蛋
                output.accept(ModItems.COOK_BLACK_GOOSE_EGG.get());       //煎黑金蛋
                output.accept(ModItems.COOK_WHITE_GOOSE_EGG.get());       //煎白玉蛋
                output.accept(ModItems.COOK_TORTOISE_EGG.get());       //煎tortoise乌龟蛋
                output.accept(ModItems.COOK_ALLIGATOR_EGG.get());       //煎alligator短吻鳄
                output.accept(ModItems.COOK_ASTRO_EGG.get());       //煎austro南方盗龙蛋
                output.accept(ModItems.COOK_BARINA_EGG.get());       //煎barina巴里纳斯鳄蛋
                output.accept(ModItems.COOK_BRACHI_EGG.get());       //煎brachi蜿龙蛋
                output.accept(ModItems.COOK_HWACHA_EGG.get());       //煎cook_hwacha_egg 箭炮猎龙蛋
                output.accept(ModItems.COOK_COTY_EGG.get());       //煎coty杯鼻龙蛋
                output.accept(ModItems.COOK_KENTRO_EGG.get());       //煎kentro肯式龙蛋
                output.accept(ModItems.COOK_MAJUNGA_EGG.get());       //煎majunga玛君龙蛋
                output.accept(ModItems.COOK_PACHY_EGG.get());       //煎pachy肿头龙蛋 
                output.accept(ModItems.COOK_RAPTRO_EGG.get());       //煎raptro伶盗龙蛋
                output.accept(ModItems.COOK_REX_EGG.get());       //煎rex霸王龙蛋
                output.accept(ModItems.COOK_MEGALA_EGG.get());       //煎megala古巨蜥蛋
                output.accept(ModItems.COOK_ANTARCTO_EGG.get());       //煎antarcto南极甲龙蛋
                output.accept(ModItems.COOK_TRIKE_EGG.get());       //煎trike三角龙蛋
                output.accept(ModItems.COOK_ANURO_EGG.get());       //煎anuro蛙嘴龙蛋 
                output.accept(ModItems.COOK_ULUGH_EGG.get());       //煎ulugh乌鲁伯戈龙蛋
                output.accept(ModItems.COOK_TALPANAS_EGG.get());       //煎talpanas鼹鸭蛋
                output.accept(ModItems.GROTTOSCERATOPS_AMLETTE.get());     
                output.accept(ModItems.RELICHEIRUS_AMLETTE.get());     
                output.accept(ModItems.SUBTERRANODON_AMLETTEX.get());    
                output.accept(ModItems.TREMORSAURUS_AMLETTE.get());     
                output.accept(ModItems.TREMORZILLA_AMLETTE_ANIM.get());     
                output.accept(ModItems.VALLUMRAPTOR_AMLETTE.get());     
                output.accept(ModItems.ATLATITAN_AMLETTE.get());   
                output.accept(ModItems.AMMLET_FROM_EMBRYO.get());     
                output.accept(ModItems.RED_CABBAGE_LEAF.get());//红卷心菜叶
                output.accept(ModItems.RED_PINEAPPLE_SLICE.get());//红凤梨切块
                output.accept(ModItems.AGLIO_ROSSO_DI_SULMONA_CLOVES.get()); //
                output.accept(ModItems.AGLIO_BIANCO_POLESANO_CLOVES.get());  //
                output.accept(ModItems.BEEFSTEAK_TOMATO_SLICE.get());  //
                output.accept(ModItems.BUTTERHEAD_LETTUCE_LEAF.get());
                output.accept(ModItems.LABA_GARLIC.get());//腊八蒜
                output.accept(ModItems.RUSSIA_BLACK_BREAD.get());       //俄罗斯黑面包
                output.accept(ModItems.GUMMY_BEAR.get());       //柑橘味小熊软糖
                output.accept(ModItems.FORESTERS_TEA.get());      //林业员茶饮  饮品
                output.accept(ModItems.GREGTECH_EMPLOYEE_TEA.get()); 
                output.accept(ModItems.INDUSTRIALIST_TEA.get()); 
                output.accept(ModItems.HIGH_ENERGY_PHYSICIST_TEA.get()); 
                output.accept(ModItems.NUCLEAR_ENGINEER_TEA.get()); 
                output.accept(ModItems.THERMAL_ENGINEER_TEA.get()); 
                output.accept(ModItems.ECO_LAB_RESEARCHER_TEA.get()); 
                output.accept(ModItems.TINKERS_TEA.get()); 
                output.accept(ModItems.ENGINEERS_TEA.get()); 
                output.accept(ModItems.JELLYFISH_SODA.get());
                output.accept(ModItems.TREESTAR_TEA.get());
                output.accept(ModItems.ADZUKI_MILK_TEA.get()); 
                output.accept(ModItems.MINT_GREEN_TEA.get()); 
                output.accept(ModItems.MOCHA_COFFEE.get());
                output.accept(ModItems.SNOW_TOP_BLACK_TEA.get()); 
                output.accept(ModItems.SNOW_TOP_COFFEE.get()); 
                output.accept(ModItems.SNOW_TOP_GREEN_TEA.get());
                output.accept(ModItems.SNOW_TOP_YELLOW_TEA.get()); 
                output.accept(ModItems.VANILLA_MILK_TEA.get()); 
                output.accept(ModItems.BLACK_TEA_ICE_CREAM.get());
                output.accept(ModItems.GREEN_TEA_ICE_CREAM.get());
                output.accept(ModItems.YELLOW_TEA_ICE_CREAM.get());
                output.accept(ModItems.COFFEE_ICE_CREAM.get());
                output.accept(ModItems.RESPITEFUL_ICE_CREAM.get());
                output.accept(ModBlocks.PECAN_PIE.get());
                output.accept(ModBlocks.ROAST_TURKEY_BLOCK.get());
                output.accept(ModBlocks.SWEET_POTATO_CASSEROLE_BLOCK.get());
                output.accept(ModItems.PECAN_PIE_SLICE.get());
                output.accept(ModItems.SWEET_POTATO_CASSEROLE.get());
                output.accept(ModItems.ROAST_TURKEY.get());
                output.accept(ModItems.STUFFING.get());
                output.accept(ModItems.MASHED_POTATOES_AND_GRAVY.get());
                //output.accept(ModBlocks..get());
                //output.accept(ModBlocks..get());
                //output.accept(ModItems..get());
                output.accept(ModItems.PASTA_WITH_TRILOCARIS_TAIL.get());
                output.accept(ModItems.TRIPODFISH_SOUP.get());
                output.accept(ModItems.URANIUM_SOUP.get());
                output.accept(ModItems.PIZZA_PROSCIUTTO_RAW.get());       //生披萨
                output.accept(ModItems.PIZZA_FUNGHI_RAW.get());       
                output.accept(ModItems.PIZZA_MARGHERITA_RAW.get());       
                output.accept(ModItems.PIZZA_DANTE_RAW.get());       
                output.accept(ModItems.PIZZA_DIAVOLA_RAW.get());       
                output.accept(ModBlocks.PIZZA_PROSCIUTTO.asItem());
                output.accept(ModBlocks.PIZZA_FUNGHI.asItem());
                output.accept(ModBlocks.PIZZA_MARGHERITA.asItem());
                output.accept(ModBlocks.PIZZA_DANTE.asItem());
                output.accept(ModBlocks.PIZZA_DIAVOLA.asItem());
                output.accept(ModItems.PIZZA_PROSCIUTTO_SLICE.get());       
                output.accept(ModItems.PIZZA_FUNGHI_SLICE.get());
                output.accept(ModItems.PIZZA_MARGHERITA_SLICE.get());       
                output.accept(ModItems.PIZZA_DANTE_SLICE.get());       
                output.accept(ModItems.PIZZA_DIAVOLA_SLICE.get());
                output.accept(ModItems.BACON_BURGER.get());
                output.accept(ModItems.BASIC_BURGER.get());
                output.accept(ModItems.CHICKEN_BURGER.get());
                output.accept(ModItems.DOUBLE_PATTY_BURGER.get());
                output.accept(ModBlocks.BLACK_TEA_CAKE.asItem());
                output.accept(ModBlocks.GREEN_TEA_CAKE.asItem());
                output.accept(ModBlocks.YELLOW_TEA_CAKE.asItem());
                output.accept(ModItems.BLACK_TEA_CAKE_SLICE.get());
                output.accept(ModItems.GREEN_TEA_CAKE_SLICE.get());
                output.accept(ModItems.YELLOW_TEA_CAKE_SLICE.get());
                //output.accept(ModItems..get()); 
                //output.accept(ModItems..get()); 
                //output.accept(ModItems..get());       
                //output.accept(ModItems..get()); 
                //output.accept(ModItems..get()); 
                output.accept(ModItems.BERRY_DELIGHT.get());       
                output.accept(ModItems.CIAMBALLONE.get());       
                output.accept(ModItems.CIAMBALLONE_SLICE.get());       
                output.accept(ModItems.MOZZARELLA.get());       
                output.accept(ModItems.MOZZARELLA_SLICES.get());       
                output.accept(ModItems.PANDORO_DOUGH.get());       
                output.accept(ModItems.PANDORO.get());     
                output.accept(ModItems.PANDORO_SLICE.get());       
                output.accept(ModItems.PANETTONE.get());       
                output.accept(ModItems.PANETTONE_SLICE.get());
                output.accept(ModItems.TIRAMISU.get());       
                output.accept(ModItems.BRUSCHETTA.get()); 
                output.accept(ModItems.MOZZARELLA_SALAD.get());       
                output.accept(ModItems.PASTA_AL_PESTO.get());    
                output.accept(ModItems.PASTA_AL_TOMATO.get());   
                output.accept(ModItems.RISOTTO_AL_FUNGGHI.get()); 
                output.accept(ModItems.RISOTTO_AL_SUGO.get());
                output.accept(ModItems.SALAME_SEAWEED_MIX.get());       
                output.accept(ModItems.SALAME_WHOLE.get()); 
                output.accept(ModItems.SALAME.get());
                output.accept(ModItems.SALAMI_SLICES.get());       
                output.accept(ModItems.BERRY_WINE.get()); 
                output.accept(ModItems.DRAGONBORN_WINE.get());       
                output.accept(ModItems.GLOWBERRY_WINE.get());       
                output.accept(ModItems.HASTING_WINE.get());       
                output.accept(ModItems.HELLHOUND_WINE.get()); 
                output.accept(ModItems.ROSE_WINE.get()); 
                output.accept(ModItems.SPUMANTE.get());       
                output.accept(ModItems.WINE_PRE_SUBSTANCE.get()); 
                output.accept(ModItems.GOKIBURI_YAKI.get()); //蟑螂乐事？
                output.accept(ModItems.JUICY_ROAST_ROACH.get());
                output.accept(ModItems.PROTEIN_BLOCK.get());
                output.accept(ModItems.ROACH_BURGER.get());
                output.accept(ModItems.ROACH_PATTY.get());
                output.accept(ModItems.ROACH_ROLL.get());
                output.accept(ModItems.ROACH_SALAD_PLATTER.get());
                output.accept(ModItems.ROACH_SANDWICH.get());
                output.accept(ModItems.ROACH_SKEWER.get());
                output.accept(ModItems.ROACH_WHISKER_CANDY.get());
                output.accept(ModItems.ROACH_MEATBALL.get());  
                output.accept(ModItems.KANGFU_XIN_YE.get());
                output.accept(ModItems.BOTTLED_ROACH.get());
                output.accept(ModItems.ALL_SMOGUS.get());                    //格雷科技食物优选
                output.accept(ModItems.BERRY_MEDLEY.get());   
                output.accept(ModItems.MULTI_SMOGUS.get());       
                output.accept(ModItems.SMOGUS.get()); 
                output.accept(ModItems.SMORE.get());   
                output.accept(ModItems.CHUM_BURGER.get());       
                output.accept(ModItems.MEAT_BURGER.get()); 
                output.accept(ModItems.CHEESE_BURGER.get()); 
                output.accept(ModItems.VEGGIE_BURGER.get()); 
                output.accept(ModItems.VEGGIE_SANDWICH.get());       
                output.accept(ModItems.CHEESE_SANDWICH.get());    
                output.accept(ModItems.BACON_BURGER.get());
                output.accept(ModItems.STEAK_BURGER.get());
                output.accept(ModItems.BACON_SANDWICH.get());
                output.accept(ModItems.LARGE_CHEESE_SANDWICH.get());
                output.accept(ModItems.LARGE_VEGGIE_SANDWICH.get()); 
                output.accept(ModItems.STEAK_SANDWICH.get()); 
                output.accept(ModItems.TOAST_SANDWICH.get()); 
                output.accept(ModItems.LARGE_BACON_SANDWICH.get()); 
                output.accept(ModItems.LARGE_STEAK_SANDWICH.get()); 
                output.accept(ModItems.GORGONZOLA_CHEESE_WHEEL.get()); 
                output.accept(ModItems.GORGONZOLA_CHEESE_SLICE.get());       
                output.accept(ModItems.GRAHAM_CRACKER.get());  
                output.accept(ModItems.CHUM.get());
                output.accept(ModItems.FRIED_POTATO_SLICE.get());    
                output.accept(ModItems.FRIED_NAQUADAH_POTATO_SLICE.get());    
                output.accept(ModItems.TUNGSTEN_STEEL_APPLE.get()); 
                output.accept(ModItems.APPLE_HARD_CANDY.get());       
                output.accept(ModItems.CORED_APPLE.get()); 
                output.accept(ModItems.BAGUETTE.get());       
                output.accept(ModItems.UNBAKED_BAGUETTE.get()); 
                output.accept(ModItems.RAW_CHEESE_PIZZA.get()); 
                output.accept(ModItems.RAW_MINCE_MEAT_PIZZA.get()); 
                output.accept(ModItems.RAW_OLIVE_MUSHROOM_PIZZA.get());     
                output.accept(ModBlocks.CHEESE_PIZZA.get());       
                output.accept(ModBlocks.MINCE_MEAT_PIZZA.get());
                output.accept(ModBlocks.OLIVE_MUSHROOM_PIZZA.get());
                output.accept(ModItems.CHEESE_PIZZA_SLICE.get()); 
                output.accept(ModItems.MINCE_MEAT_PIZZA_SLICE.get());
                output.accept(ModItems.OLIVE_MUSHROOM_PIZZA_SLICE.get());
                output.accept(ModItems.MINCE_MEAT.get()); 
                output.accept(ModItems.COOKED_MINCE_MEAT.get()); 
                output.accept(ModItems.MEAT_INGOT.get()); 
                output.accept(ModItems.COOKED_MEAT_INGOT.get()); 
                output.accept(ModItems.COOKED_SAUSAGE.get()); 
                output.accept(ModItems.SAUSAGE.get()); 
                output.accept(ModItems.FERMENTED_CHORUS.get()); 
                output.accept(ModItems.FERMENTED_CHORUS_PIE.get());
                output.accept(ModItems.FERMENTED_CHORUS_PIE_SLICE.get());
                output.accept(ModItems.PORCHETTA.get());
                output.accept(ModItems.PORCHETTA_SLICE.get());
                output.accept(ModItems.BAKED_BEANS_TOAST.get());
                output.accept(ModItems.BAKED_BEANS.get());
                output.accept(ModItems.GELATIN.get()); 
                output.accept(ModItems.PEA_PASTE.get()); 
                output.accept(ModItems.SYALS.get());       
                output.accept(ModItems.KETTLE_CHIPS.get()); 
                output.accept(ModItems.REDUCED_FAT_CHIPS.get());       
                output.accept(ModItems.BAG_O_CHIPS.get()); 
                output.accept(ModItems.VINEGAR_CHIPS.get());  
                output.accept(ModItems.NAQUADAH_CHIPS.get());

                //output.accept(ModItems..get());       
                //output.accept(ModItems..get());
                output.accept(ModItems.RAW_BARG_KEBAB.get());
                output.accept(ModItems.RAW_CARROT_KEBAB.get());
                output.accept(ModItems.RAW_CHUM_KEBAB.get());
                output.accept(ModItems.RAW_KUBIDEH_KEBAB.get());
                output.accept(ModItems.RAW_MEAT_KEBAB.get());
                output.accept(ModItems.RAW_ONION_KEBAB.get());
                output.accept(ModItems.RAW_TAILFAT_KEBAB.get());
                output.accept(ModItems.RAW_TOMATO_KEBAB.get());
                output.accept(ModItems.BARG_KEBAB.get());
                output.accept(ModItems.CARROT_KEBAB.get());
                output.accept(ModItems.CHUM_KEBAB.get());
                output.accept(ModItems.CHUM_WITH_STICK.get());
                output.accept(ModItems.KEBAB_E_SOLTANI.get());
                output.accept(ModItems.KUBIDEH_KEBAB.get());
                output.accept(ModItems.MEAT_KEBAB.get());
                output.accept(ModItems.ONION_KEBAB.get());
                output.accept(ModItems.TAILFAT_KEBAB.get());
                output.accept(ModItems.TOMATO_KEBAB.get());
                output.accept(ModItems.BAKED_CAKE_BOTTOM.get());
                output.accept(ModItems.BATCH_FRIED_POTATO_SLICE.get());
                output.accept(ModItems.CAPSULE_BODY.get());
                output.accept(ModItems.CAPSULE_HAT.get());
                output.accept(ModItems.CORNCOB.get());
                output.accept(ModItems.DITALINI.get());
                output.accept(ModItems.DRIED_CORN.get());
                output.accept(ModItems.DRIED_DITALINI.get());
                output.accept(ModItems.DRIED_LASAGNA.get());
                output.accept(ModItems.DRIED_RIGATONI.get());
                output.accept(ModItems.DRIED_SPAGHETTI.get());
                output.accept(ModItems.DRIED_TAGLIATELLE.get());
                output.accept(ModItems.EGG_PASTA_DOUGH.get());
                output.accept(ModItems.FLAT_DOUGH.get());
                output.accept(ModItems.FRENCH_FRY.get());
                output.accept(ModItems.FRIED_POTATO_STRIP.get());
                output.accept(ModItems.GRAHAM_DOUGH.get());
                output.accept(ModItems.GREASY_POTATO_SLICE.get());
                output.accept(ModItems.HOT_GRAHAM_CRACKER_BASE.get());
                output.accept(ModItems.HOT_GRAHAM_DOUGH.get());
                output.accept(ModItems.HOT_POTATO_SLICE.get());
                output.accept(ModItems.LASAGNA.get());
                output.accept(ModItems.LOW_FAT_POTATO_SLICE.get());
                output.accept(ModItems.PASTA_DOUGH.get());
                output.accept(ModItems.PEELED_POTATO.get());
                output.accept(ModItems.PIE_CRUST.get());
                output.accept(ModItems.POTATO_SLICE.get());
                output.accept(ModItems.POTATO_STRIP.get());
                output.accept(ModItems.RAW_JIAOZI.get());
                output.accept(ModItems.RAW_PELMENI.get());
                output.accept(ModItems.RIGATONI.get());
                output.accept(ModItems.SPAGHETTI.get());
                output.accept(ModItems.TAGLIATELLE.get());
                output.accept(ModItems.TORTELLINI.get());
                output.accept(ModItems.UNBAKED_BREAD.get());
                output.accept(ModItems.UNBAKED_BUN.get());
                output.accept(ModItems.UNBAKED_CAKE_BOTTOM.get());
                output.accept(ModItems.UNFILTERED_GRAHAM_CRACKER.get());
                output.accept(ModItems.BRITISH_BREAKFAST.get());
                output.accept(ModItems.BREAD_SLICE.get());
                output.accept(ModItems.BEEF_SLICE.get());
                output.accept(ModItems.BEANS_WITH_TOMATO_SAUSE.get());
                output.accept(ModItems.BAGGED_POPCORN.get());
                output.accept(ModItems.APPLE_HARD_CANDY_PLATE.get());
                output.accept(ModItems.APPLE_HARD_CANDY_FONDANT.get());
                output.accept(ModItems.ANIMAL_FAT.get());
                output.accept(ModItems.FISH_AND_CHIPS.get());
                output.accept(ModItems.CRASHED_APPLE_HARD_CANDY.get());
                output.accept(ModItems.COOKED_BEEF_SLICE.get());
                output.accept(ModItems.COOKED_SAUSAGE_ROLL.get());
                output.accept(ModItems.FRIED_FISH.get());
                output.accept(ModItems.FRENCH_FRIES.get());
                output.accept(ModItems.HOT_APPLE_HARD_CANDY_PLATE.get());
                output.accept(ModItems.HOT_GRAHAM_CRACKER.get());
                output.accept(ModItems.FLAVOURED_PELMENI.get());
                output.accept(ModItems.JIAOZI.get());
                output.accept(ModItems.MARSHMALLOW.get());
                output.accept(ModItems.MASHED_POTATO.get());
                output.accept(ModItems.MELTING_MILK_CHOCOLATE.get());
                output.accept(ModItems.MILK_CHOCOLATE.get());
                output.accept(ModItems.PELMENI.get());
                output.accept(ModItems.POPCORN.get());
                output.accept(ModItems.TOAST.get());
                output.accept(ModItems.SPOILED_MEAT.get());
                output.accept(ModItems.SHEPHERDS_PIE.get());
                output.accept(ModItems.SAUSAGE_ROLL.get());
                output.accept(ModItems.COAC.get());
                output.accept(ModItems.SEPIP.get());
                output.accept(ModItems.GTFO_PURPLE_DRINK.get());
                output.accept(ModItems.ANTAF.get());
                output.accept(ModItems.CRANBERRY_ETRIPS.get());
                output.accept(ModItems.ORANGE_JUICE.get());   
                output.accept(ModItems.APPLE_JUICE.get());   
                output.accept(ModItems.NAQUADAH_MILK.get());
                output.accept(ModItems.LENINADE.get()); 
                output.accept(ModItems.VODKA.get());       
                output.accept(ModItems.PURPLE_DRINK.get()); 
                output.accept(ModItems.ETRIPS.get());       
                output.accept(ModItems.SPARKLING_WATER.get());
                output.accept(ModItems.BEER.get());   
                output.accept(ModItems.QINGDAO_BEER.get());  
                //胶囊 GTFO守门员
                output.accept(ModItems.FERMENTED_CHORUS_CAPSULE.get()); 
                output.accept(ModItems.GELATIN_CAPSULE.get()); 
                output.accept(ModItems.PARACETAMOL_CAPSULE.get());
                output.accept(ModItems.VIBRANT_CAPSULE.get()); 
                output.accept(ModItems.PLUTONIUM_241_CAPSULE.get()); 
                //黑刺李浆果 马斯卡葡萄 榅桲 氧葡萄 棱珠 诡异挂果 杨桃 榴莲 柚子 哈密瓜 黑莓 覆盆子 黑加仑 醋栗 车厘子 橄榄 李子 杏子 青梅 青提 百香果 火龙果 红心火龙果 冬青果 
                //山楂 石榴 无花果 牛油果 西柚 荔枝 木瓜 灯笼果 姬海棠 蛋黄果 朽老苹果 柿子 山竹 杨梅 椰枣
                //佛手柑 嘉宝果 火参果 红毛丹 海葡萄 杜鹃莓 野莓 金桔 香蕉 暗紫浆果 接骨木莓
                //巴坦木 开心果 松子 板栗 腰果 榛子 杏仁 核桃      
                //腐芋 香芋 
                //三叶虾 恐龙 帝王蟹 鲍鱼 龙虾 鹿肉 山羊肉 野猪肉 野牛肉
                //
                output.accept(ModItems.SPECIAL_WHEAT_FLOUR.get());  //特制面粉
                //output.accept(ModItems..get());  //莲蓉和咸蛋黄
                //output.accept(ModItems..get());  //果仁和火腿
                //output.accept(ModItems..get());  //黑芝麻和豆沙
                //output.accept(ModItems..get());  //糯米和椰蓉
                //output.accept(ModItems..get());  //莲蓉双黄月饼
                output.accept(ModItems.FIVE_KERNALS_HAM_MOONCAKE.get());  //五仁火腿月饼
                output.accept(ModItems.BLACK_SESAME_RED_BEAN_PASTE_MOONCAKE.get());  //芝麻豆沙月饼
                output.accept(ModItems.SNOW_SKIN_COCONUT.get());  //冰皮椰蓉月饼    
                //output.accept(ModItems..get()); 
                //output.accept(ModItems..get()); 
                //output.accept(ModItems..get());       
                //output.accept(ModItems..get());
                //output.accept(ModItems..get());      
                //output.accept(ModItems..get());       
                output.accept(ModItems.BLACK_BURGER.get());       //黑麦汉堡
                output.accept(ModItems.BLACK_BURGER_BOTTOM.get());       //黑麦汉堡底
                output.accept(ModItems.BLACK_BURGER_TOP.get());       //黑麦汉堡顶
                output.accept(ModItems.BARLEY_BURGER.get());       //大麦汉堡
                output.accept(ModItems.BARLEY_BURGER_BOTTOM.get());       //大麦汉堡底
                output.accept(ModItems.BARLEY_BURGER_TOP.get());       //大麦汉堡顶
                output.accept(ModItems.CASSAVA_BURGER.get());       //木薯汉堡
                output.accept(ModItems.CASSAVA_BURGER_BOTTOM.get());       //木薯汉堡底
                output.accept(ModItems.CASSAVA_BURGER_TOP.get());       //木薯汉堡顶
                output.accept(ModItems.CORN_BURGER.get());       //玉米汉堡
                output.accept(ModItems.CORN_BURGER_BOTTOM.get());       //玉米汉堡底
                output.accept(ModItems.CORN_BURGER_TOP.get());       //玉米汉堡顶
                output.accept(ModItems.EVOLUTCORN_BURGER.get());       //白垩玉藜堡 
                output.accept(ModItems. EVOLUTCORN_BURGER_BOTTOM.get());       //白垩玉藜堡底
                output.accept(ModItems.EVOLUTCORN_BURGER_TOP.get());       //白垩玉藜堡顶
                output.accept(ModItems.KWAT_WHEAT_BURGER.get());       //瓦斯麦堡 
                output.accept(ModItems.KWAT_WHEAT_BURGER_BOTTOM.get());       //瓦斯麦堡底
                output.accept(ModItems.KWAT_WHEAT_BURGER_TOP.get());       //瓦斯麦堡顶
                output.accept(ModItems.BLACK_OAK_BURGER.get());         //黑橡子
                output.accept(ModItems.BLACK_OAK_BURGER_BOTTOM.get());             //黑橡子
                output.accept(ModItems.BLACK_OAK_BURGER_TOP.get());                //黑橡子
                output.accept(ModItems.OAK_BURGER.get());       //橡子
                output.accept(ModItems.OAK_BURGER_BOTTOM.get());       //橡子
                output.accept(ModItems.OAK_BURGER_TOP.get());       //橡子
                output.accept(ModItems.SWEET_POTATO_BURGER.get());       //红薯
                output.accept(ModItems.SWEET_POTATO_BURGER_BOTTOM.get());       //红薯
                output.accept(ModItems.SWEET_POTATO_BURGER_TOP.get());       //红薯
                output.accept(ModItems.PUMPKIN_BURGER.get());       //南瓜
                output.accept(ModItems.PUMPKIN_BURGER_BOTTOM.get());       //南瓜
                output.accept(ModItems.PUMPKIN_BURGER_TOP.get());       //南瓜
                output.accept(ModItems.NUT_BURGER.get());       //坚果
                output.accept(ModItems.NUT_BURGER_BOTTOM.get());       //坚果
                output.accept(ModItems.NUT_BURGER_TOP.get());       //坚果
                output.accept(ModItems.GHAST_BURGER.get());       //恶魂
                output.accept(ModItems.GHAST_BURGER_BOTTOM.get());       //恶魂
                output.accept(ModItems.GHAST_BURGER_TOP.get());       //恶魂
                output.accept(ModItems.BANANA_BURGER.get());       //香蕉
                output.accept(ModItems.BANANA_BURGER_BOTTOM.get());       //香蕉
                output.accept(ModItems.BANANA_BURGER_TOP.get());       //香蕉
                output.accept(ModItems.POTATO_BURGER.get());       //马铃薯
                output.accept(ModItems.POTATO_BURGER_BOTTOM.get());       //马铃薯
                output.accept(ModItems.POTATO_BURGER_TOP.get());       //马铃薯
                output.accept(ModItems.DIAMOND_LATTICE_POTATO_BURGER.get());       //晶格马铃薯
                output.accept(ModItems.DIAMOND_LATTICE_POTATO_BURGER_BOTTOM.get());       //晶格马铃薯
                output.accept(ModItems.DIAMOND_LATTICE_POTATO_BURGER_TOP.get());       //晶格马铃薯
                output.accept(ModItems.NEUTRONIUM_BURGER.get());       //简并态汉堡
                output.accept(ModItems.NEUTRONIUM_BURGER_BOTTOM.get());       //简并态汉堡底
                output.accept(ModItems.NEUTRONIUM_BURGER_TOP.get());       //
                output.accept(ModItems.NEUTRONIUM_BUTTER.get());       //中子素黄油
                output.accept(ModItems.RYE_DOUGH.get());
                output.accept(ModItems.BARLEY_DOUGH.get());
                output.accept(ModItems.GIANT_CLAM_MEAT.get());     //砗磲肉
                output.accept(ModItems.CRAB_MEAT.get());   //蟹肉
                output.accept(ModItems.WAGYU_BEEF.get());
                output.accept(ModItems.GOKIBURI_YAKI.get()); //蟑螂乐事？
                output.accept(ModItems.JUICY_ROAST_ROACH.get());
                output.accept(ModItems.PROTEIN_BLOCK.get());
                output.accept(ModItems.ROACH_BURGER.get());
                output.accept(ModItems.ROACH_PATTY.get());
                output.accept(ModItems.ROACH_ROLL.get());
                output.accept(ModItems.ROACH_SALAD_PLATTER.get());
                output.accept(ModItems.ROACH_SANDWICH.get());
                output.accept(ModItems.ROACH_SKEWER.get());
                output.accept(ModItems.ROACH_WHISKER_CANDY.get());
                output.accept(ModItems.ROACH_MEATBALL.get());
                //output.accept(ModItems..get());
                //output.accept(ModItems..get());
                
            })
            .build()
    );
 
                /*
                我是真的没想到啊
                几年前的大饼居然在这里去做了
                不过优先统计特殊I和II的食物
                特殊I：不可制作
                特殊II：可以制作但是限定

                I不可制作：
                大神爱心慕斯蛋糕、可乐小子饼干、树根蛋糕、彩虹天堂-零卡、彩虹天堂、超豪华鸡腿便当、干净的罐头
                西瓜冰沙、牛奶雪糕、Q软彩虹糖
                七巧五子拼盘、甜蜜七巧果、浓情巧克力慕斯、恋语棉花糖、爱心甜甜圈
                生日蛋糕、爱心巧克力
                手工饺子、思念彩虹糖
                自制石头烤肉、飘香荷叶烤鸡、冰爽莓莓                   来源：周末巡逻-克里斯
                活力热狗、铁板鱿鱼、午后巡逻套餐                       来源：周末巡逻-海德尔
                销魂叉烧饭、焦糖布丁、青草地甜筒、虾尾拌面、芥末虾球    来源：周末巡逻-巴格克星
                派对拐杖糖、赤郡奶酪、肥仔米、滋补椰子鸡               来源：周末巡逻-朵洛希·哈维
                秘制油泼面、海鲜烩饭、绵绵泡芙、香甜柿饼               来源：周末巡逻-海伦娜·薇恩
                珍珠丸子、红枣糕、甜水面、肉酱千层面                   来源：周末巡逻-海德尔
                三鲜冰粉、糯米糖藕                                    来源：周末巡逻-哈提  
                秘制烤肉              
                糖年糕、莓果蛋糕、百物炖肉菜、金银满屋、雪人糯米滋
                新禧春卷、年年有鱼、吉祥年糕、五福饺子             来源：历年春节活动
                庆典蛋糕                                         来源：周年活动
                杏仁布丁、圣诞姜饼、奶油玉米浓汤
                汉堡王狂欢套餐、大士神仙水                        来源：联动
                爱意巧克力、白巧克力
                嫩烤香草羊排
                九转大肠
                好事者的玩笑
                染毒烤肉串、整蛊果子、快过期的罐头
                方便面、奇怪的方便面
                海鲜全家福、海陆豪华大餐
                四叶草抹茶慕斯、四叶草鲜奶油蛋糕、幸运香草舒芙蕾、幸福四叶草巴菲、四叶草彩虹蛋糕、幸运香草马克龙
                糖霜李子、枫叶棒棒糖、紫藤花茶、樱花果冻

                II可以制作但是限定
                麻辣肉片、鸳鸯鱼丸、养生蔬菜
                椒盐藕饼、藕粉羹
                金鱼果汤、金!鱼果汤
                惊奇南瓜切片
                菊花茶
                白条鱼生、烤赤魟、烤针鱼、油炸雨鱼
                魔鬼鱼与小丑鱼料理

                元宵节
                中秋节
                端午节
                感恩节
                */

    // 农作物标签页
    public static final RegistryObject<CreativeModeTab> CROPS_TAB = CREATIVE_TABS.register("crops_crops", () ->
            CreativeModeTab.builder()
                    .title(REGISTRATE.addLang("itemGroup", ListMod.rl("crops_crops"), "Crops"))  // 语言键: itemGroup.listmod.crops
                    .icon(() -> new ItemStack(ModItems.RED_ONION.get()))
                    .displayItems((parameters, output) -> {
                    //output.accept(ModItems.SPRING_BAMBOO_SHOOT.get());     // 春笋   //明日之后蔬菜
                    //output.accept(ModItems.HAIRY_BAMBOO_SHOOT.get());      // 毛笋
                    //output.accept(ModItems.LOTUS_ROOT.get());              // 莲藕
                    //output.accept(ModItems.GLUTINOUS_RICE.get());          // 糯米
                    //output.accept(ModItems.MUNG_BEAN.get());               // 绿豆
                    //output.accept(ModItems.SESAME_SEED.get());             // 芝麻
                    //output.accept(ModItems.RAPESEED.get());                // 油菜
                    //output.accept(ModItems.CHRYSANTHEMUM.get());           // 菊花
                    output.accept(ModItems.HARSH_WINTER_STRAWBERRY.get());       //凛冬草莓
                    //output.accept(ModItems.PARSNIP.get());                 // 防风草     //蔬菜 星露谷
                    //output.accept(ModItems.SNOW_YAM.get());                // 雪山药
                    //output.accept(ModItems.WHITE_RADISH.get());            // 白萝卜
                    //output.accept(ModItems.AMARANTH.get());                // 苋菜
                    output.accept(ModItems.RED_CABBAGE.get());               //红叶卷心菜
                    //output.accept(ModItems.GOLDEN_ZUCCHINI.get());  //金皮西葫芦
                    //output.accept(ModItems.ARTICHOKE.get());        //洋蓟
                    //output.accept(ModItems.BOK_CHOY.get());         //小白菜
                    //output.accept(ModItems.YAM.get());              //山药
                    //output.accept(ModItems.FROSTED_MELON.get());    //霜瓜
                    //output.accept(ModItems.CABBAGE.get());          //甘蓝菜
                    //output.accept(ModItems.WINTER_ROOT.get());      //冬根
                    //output.accept(ModItems.QI_GUAN.get());          //齐瓜
                    //output.accept(ModItems.WILD_WASABI.get());       //野山葵
                    //output.accept(ModItems.WASABI_ROOT.get());       //山葵根       //蔬菜 星露谷聚宝盆mod
                    //output.accept(ModItems.HOROPPA.get());           //胡罗巴
                    //output.accept(ModItems.MUSTARD.get());           //芥菜
                    //output.accept(ModItems.OREGANO.get());           //牛至
                    //output.accept(ModItems.CHIVES.get());            //细香葱
                    //output.accept(ModItems.OKRA.get());              //秋葵
                    //output.accept(ModItems.THYMUS.get());            //百里香
                    //output.accept(ModItems.CATNIP.get());            //猫薄荷
                    //output.accept(ModItems.BAY_LEAF.get());          //月桂叶
                    //output.accept(ModItems.LENTIL.get());            //扁豆
                    //output.accept(ModItems.FENNEL.get());            //小茴香
                    //output.accept(ModItems.CILANTRO.get());          //香菜
                    //output.accept(ModItems.ROSEMARY.get());          //迷迭香
                    //output.accept(ModItems.DILL.get());              //莳萝
                    //output.accept(ModItems.CELERY.get());            //芹菜
                    //output.accept(ModItems.TARRAGON.get());          //龙蒿
                    //output.accept(ModItems.BEAN_SPROUT.get());       //豆芽
                    //output.accept(ModItems.ALOE.get());              //芦荟
                    //output.accept(ModItems.WORMWOOD.get());          //苦艾
                    //output.accept(ModItems.RAPESEED_FLOWER.get());   //油菜花
                    //output.accept(ModItems.PERILLA_LEAF.get());      //紫苏叶
                    output.accept(ModItems.PEA_POD.get());
                    output.accept(ModItems.PEA.get());
                    //output.accept(ModItems.RED_BEAN.get());          //赤豆
                    //output.accept(ModItems.BASIL.get());             //罗勒
                    output.accept(ModItems.RED_ONION.get());           //红洋葱
                    //output.accept(ModItems.WHITE_ONION.get());       //白洋葱
                    //output.accept(ModItems.SPEARMINT.get());            //香花薄荷
                    //output.accept(ModItems.AGAVE.get());                //龙舌兰
                    //output.accept(ModItems.BLUE_AGAVE.get());           //蓝龙舌兰
                    //output.accept(ModItems.SHALLOT.get());              //红葱头（红葱头一般叫红毛葱）
                    //output.accept(ModItems.RED_KIDNEY_BEAN.get());      //红腰豆（有毒）
                    //output.accept(ModItems.NAVY_BEAN.get());            //海军豆
                    //output.accept(ModItems.PURPLE_YAM.get());           //紫山药
                    //output.accept(ModItems.LICORICE_ROOT.get());        //洋甘草根
                    //output.accept(ModItems.HAVANA_CHILI.get());         //哈瓦那辣椒
                    //output.accept(ModItems.ASPARAGUS.get());            //芦笋
                    //output.accept(ModItems.CREAM_PUMPKIN.get());        //奶油南瓜
                    //output.accept(ModItems.QUINOA.get());               //藜麦
                    // output.accept(ModItems.BUCKWHEAT.get());            //荞麦
                    //output.accept(ModItems.NUTMEG.get());               //肉豆蔻
                    //output.accept(ModItems.SAGE.get());                 //鼠尾草
                    output.accept(ModItems.GINSENG.get());              //人参
                    output.accept(ModItems.LAYER_TREE_LEAF.get());      //千层树叶
                    output.accept(ModItems.CAMPHOR_LEAF.get());         //樟树叶
                    output.accept(ModItems.EUCALYPTUS_LEAF.get());      //桉树叶
                    output.accept(ModItems.ANCIENT_FRUIT.get());          //上古水果（星露谷水果，反正分不清了就这样吧）
                    output.accept(ModItems.GEMBERRY.get());               //宝石甜莓
                    output.accept(ModItems.CRYSTAL_FRUIT.get());          //水晶果
                    output.accept(ModItems.HEART_FRUIT.get());            //人心果
                    //output.accept(ModItems.LUOHAN_ORANGE.get());          //罗汉橙
                    //output.accept(ModItems.AMERICAN_RASPBERRY.get());     //美洲大树莓
                    output.accept(ModItems.MUSHROOM_BERRY.get());         //菇娘果
                    //output.accept(ModItems.BREADFRUIT.get());             //面包果
                    //output.accept(ModItems.FRAGRANT_BERRY.get());         //香味浆果
                    //output.accept(ModItems.MUSK_MELON.get());             //香瓜
                    //output.accept(ModItems.GOLDEN_MELON.get());           //金丝甜瓜
                    output.accept(ModItems.NECTARINE.get());              //油桃
                    output.accept(ModItems.PECAN.get());                  //碧根果
                    output.accept(ModItems.YELLOW_PEACH.get());           //黄桃（明日之后 水果 真的很少）
                    //output.accept(ModItems.SEA_BUCKTHORN.get());          //沙棘果
                    //output.accept(ModItems.LONGAN.get());                 //龙眼
                    //output.accept(ModItems.MULBERRY.get());               //桑葚
                    //output.accept(ModItems.WINTER_JUJUBE.get());          //冬枣
                    //output.accept(ModItems.WATERMELON_84.get());          //84西瓜
                    //output.accept(ModItems..get());       //黑美人西瓜
                    //output.accept(ModItems..get());       //宁夏晒砂瓜
                    //output.accept(ModItems..get());       //黄金麒麟瓜 （外部金黄，黄色肉）
                    //output.accept(ModItems..get());       //金色罗黄西瓜 （黄色肉，外部绿黑）
                    //output.accept(ModItems..get());       //圣彩西瓜（外部绿黑，内部橙红色）
                    //output.accept(ModItems..get());       //赤肉甜西瓜（Crimson Sweet）
                    //output.accept(ModItems..get());       //三白瓜 （就是全白的西瓜）
                    //output.accept(ModItems..get());       //新红宝西瓜
                    //output.accept(ModItems..get());       //墨童西瓜
                    //output.accept(ModItems..get());       //日本Densuke黑皮西瓜（伝助スイカ）  英文：Japanese Densuke Black Watermelon
                    //output.accept(ModItems..get());       //打瓜 （籽瓜）
                    //output.accept(ModItems..get());       //石山西瓜
                    //output.accept(ModItems..get());       //Moon & Stars Watermelon
                    //output.accept(ModItems..get());       //非洲野西瓜 Citrullus lanatus var. citroides
                    //output.accept(ModItems..get());       //药西瓜（有毒）
                    //output.accept(ModItems.DURIO_GRAVEOLENS.get());       //红肉榴莲 拉丁学名Durio graveolens
                    //output.accept(ModItems..get());       //龟榴莲   拉丁学名Durio testudinarum
                    //output.accept(ModItems..get());       //古泰榴莲 拉丁学名Durio kutejensis
                    //output.accept(ModItems..get());       //奥氏榴莲 拉丁学名Durio oxleyanus
                    //output.accept(ModItems..get());       //大花榴莲 拉丁学名Durio grandiflorus
                    //output.accept(ModItems..get());       //黑刺榴莲 这玩意不是野生品种了
                    //output.accept(ModItems..get());       //犀牛蕉
                    //output.accept(ModItems..get());       //千指蕉
                    //output.accept(ModItems..get());       //小王子蕉
                    //output.accept(ModItems..get());       //象腿蕉
                    //output.accept(ModItems..get());       //朝天蕉  不是tmd重庆那个朝天椒
                    output.accept(ModItems.RED_DACCA_BANANA_BUNCH.get());       //南洋红香蕉
                    output.accept(ModItems.RED_DACCA_BANANA.get());
                    output.accept(ModItems.BLUE_JAVA_BANANA_BUNCH.get());       //蓝爪哇香蕉 Blue Java   题外话：FUCK JAVA!!!
                    output.accept(ModItems.BLUE_JAVA_BANANA.get());
                    output.accept(ModItems.PLANTAIN_BUNCH.get());
                    output.accept(ModItems.PLANTAIN.get());
                    //output.accept(ModItems..get());       //大麦克香蕉
                    //output.accept(ModItems..get());       //红宝石香蕉
                    //output.accept(ModItems..get());       //巨人香蕉
                    //output.accept(ModItems..get());       //Isla Banana  紫皮偏黑？  Thai Black Banana（泰黑蕉）？
                    //output.accept(ModItems..get());       //菜蕉
                    //output.accept(ModItems..get());       //皇帝蕉
                    //output.accept(ModItems..get());       //费香蕉  应该是学名fe’i banana
                    //output.accept(ModItems..get());       //野蕉（香蕉老祖）
                    //output.accept(ModItems..get());       //小果野蕉（香蕉老祖）
                    output.accept(ModItems.RED_PINEAPPLE.get());       //红菠萝 外皮红色
                    output.accept(ModItems.TAIWAN_GREEN_PINEAPPLE.get());       //台湾菠萝
                    //output.accept(ModItems..get());       //粉红菠萝 pinkglow 内部粉红
                    output.accept(ModItems.PERFUME_PINEAPPLE.get());       //香水凤梨
                    //output.accept(ModItems..get());       //假菠萝 露兜果
                    //output.accept(ModItems..get());       //黄金椰子
                    //output.accept(ModItems..get());       //海底椰
                    output.accept(ModItems.NYPA.get());       //水椰
                    //output.accept(ModItems..get());       //糖棕 Borassus flabellifer
                    //output.accept(ModItems..get());       //西谷椰子
                    //output.accept(ModItems..get());       //砂糖椰子 /桄榔
                    //output.accept(ModItems..get());       //椰柿 Quararibea cordata
                    //output.accept(ModItems..get());       //丑橘
                    //output.accept(ModItems..get());       //蜜柑 嗯...群友（（   日语みかん不过我觉得还是拉丁更权威
                    //output.accept(ModItems..get());       //红心柚
                    //output.accept(ModItems..get());       //小青柠
                    //output.accept(ModItems..get());       //手指柠檬/澳洲指橙
                    //output.accept(ModItems..get());       //苦橙 Citrus aurantium
                    //output.accept(ModItems..get());       //木橘 Aegle marmelos
                    //output.accept(ModItems..get());       //象橘 Limonia acidissima
                    output.accept(ModItems.CHOCOLAT_PERSIMMON.get());       //巧克力柿/黑肉柿
                    //output.accept(ModItems..get());       //黑柿
                    //output.accept(ModItems..get());       //德州小黑柿
                    //output.accept(ModItems..get());       //火晶柿子
                    //output.accept(ModItems..get());       //老鸦柿
                    //output.accept(ModItems..get());       //君迁子
                    //output.accept(ModItems..get());       //
                    //output.accept(ModItems..get());       //
                    //output.accept(ModItems..get());       //
                    //output.accept(ModItems..get());       //中国南瓜 Cucurbita moschata
                    //output.accept(ModItems..get());       //蓝哈伯德南瓜Blue Hubbard
                    //output.accept(ModItems..get());       //意大利青皮南瓜 Marina di Chioggia
                    //output.accept(ModItems..get());       //大西洋巨人南瓜 Cucurbita maxima, Atlantic Giant
                    //output.accept(ModItems..get());       //西非牡蛎瓜 Cucumeropsis mannii
                    //output.accept(ModItems..get());       //白沙蜜甜瓜（都白）
                    //output.accept(ModItems..get());       //羊角密瓜
                    //output.accept(ModItems..get());       //玲珑密瓜（绿肉）
                    //output.accept(ModItems..get());       //香妃密瓜（黄皮黄肉）
                    //output.accept(ModItems..get());       //白兰瓜（白皮绿肉）
                    //output.accept(ModItems..get());       //伊丽莎白（黄皮白肉）
                    //output.accept(ModItems..get());       //脆宝甜瓜（全绿的甜瓜）
                    //output.accept(ModItems..get());       //江西梨瓜（白皮绿肉的甜瓜）
                    //output.accept(ModItems..get());       //流星甜瓜（白皮白肉甜瓜）
                    //output.accept(ModItems..get());       //博洋9号密瓜
                    //output.accept(ModItems..get());       //面瓜
                    //output.accept(ModItems..get());       //花皮冬瓜
                    //output.accept(ModItems..get());       //菜瓜（烧瓜）
                    //output.accept(ModItems..get());       //冬瓜
                    //output.accept(ModItems..get());       //蛇瓜
                    //output.accept(ModItems..get());       //瓠瓜
                    //output.accept(ModItems..get());       //佛手瓜
                    //output.accept(ModItems..get());       //苦瓜
                    //output.accept(ModItems..get());       //苹果苦瓜
                    //output.accept(ModItems..get());       //丝瓜
                    //output.accept(ModItems..get());       //马泡瓜 学名Cucumis melo var. agrestis
                    //output.accept(ModItems..get());       //拇指西瓜（糙毛马㼎儿）学名Melothria scabra
                    //output.accept(ModItems..get());       //蒙特可可 Theobroma bicolor
                    output.accept(ModItems.GLYCINE_SOJA.get());
                    output.accept(ModItems.AGLIO_ROSSO_DI_SULMONA.get());
                    output.accept(ModItems.AGLIO_BIANCO_POLESANO.get());
                    output.accept(ModItems.ARTICHOKES.get());
                    output.accept(ModItems.BEEFSTEAK_TOMATO.get());
                    output.accept(ModItems.BUTTERHEAD_LETTUCE.get());
                    output.accept(ModItems.CAFONA_APRICOT.get());
                    output.accept(ModItems.CETRIOLO_IUNGO.get());
                    output.accept(ModItems.CEYLON_CINNAMON.get());
                    output.accept(ModItems.CINNAMON_FRUIT.get());
                    output.accept(ModItems.CIPOLLA_DI_CERTALDO.get());
                    output.accept(ModItems.CORATION_OLIVE.get());
                    output.accept(ModItems.DEKOPON_ORANGE.get());
                    output.accept(ModItems.GENOVESE_BASIL_LEAF.get());
                    
                    output.accept(ModItems.HORSERADISH.get());
                    output.accept(ModItems.ITALIAN_LONG_EGGPLANT.get());
                    output.accept(ModItems.KENT_MANGO.get());
                    output.accept(ModItems.MAGNUM_HOP.get());
                    output.accept(ModItems.OREGANO_LEAF.get());
                    output.accept(ModItems.SWEET_CORN.get());
                    output.accept(ModItems.LONG_GRAIN_RICE.get());
                    output.accept(ModItems.GROS_MICHEL_BANANA.get());         //大麦克香蕉
                    output.accept(ModItems.DWARF_COCONUT_FRUIT.get());       //矮种椰子果实
                    output.accept(ModItems.GIZA_COTTON.get());
                    output.accept(ModItems.NEBBIOLO_GRAPE.get());     //葡萄
                    output.accept(ModItems.FIANO_WHITE_GRAPE.get());     //葡萄
                    output.accept(ModItems.EUROPEAN_BLACKBERRY.get());       //欧洲黑莓
                    output.accept(ModItems.AMERICAN_BLACK_CURRANT.get());       //黑醋栗
                    output.accept(ModItems.ROVADA_RED_CURRANT.get());       //红醋栗
                    output.accept(ModItems.VERSAILLES_WHITE_CURRANT.get());       //白醋栗
                    output.accept(ModItems.ORIENTAL_STRAWBERRY.get());       //东方草莓
                    output.accept(ModItems.LINGONBERRY.get());       //越橘
                    output.accept(ModItems.HASCHBERG_ELDERBERRY.get());       //哈施贝格接骨木莓
                    output.accept(ModItems.LARGE_CRANBERRY.get());       //大果蔓越莓
                    output.accept(ModItems.EUROPEAN_RED_RASPBERRY.get());       //欧洲红莓
                    output.accept(ModItems.NORTHERN_HIGHBUSH_BLUEBERRY.get());       //北高从蓝莓
                    //output.accept(ModItems..get());       //
                    //output.accept(ModItems..get());       //
                    //output.accept(ModItems..get());       //
                    //output.accept(ModItems..get());       //
                    //output.accept(ModItems..get());       //
                    //output.accept(ModItems..get());       //
                    //output.accept(ModItems..get());       //
                    //output.accept(ModItems..get());       //
                    //output.accept(ModItems..get());       //
                    //output.accept(ModItems..get());       //

                    output.accept(ModItems.STRAW_MUSHROOM.get());
                    //output.accept(ModItems.香菇.get());
                    output.accept(ModItems.SEA_MUSHROOM.get());
                    output.accept(ModItems.CAVE_MUSHROOM.get());
                    //白松露

                    //output.accept(ModItems.紫蘑菇.get());
                    //output.accept(ModItems.熔岩蘑菇.get());
                    //output.accept(ModItems.鸡油菌.get());
                    //output.accept(ModItems.鸡冠菌.get())；
                    /*
                    Armillaria mellea（蜜环菌）
                    Leccinum scabrum，中文常称为“桦褐牛肝菌”
                    Russula vesca（中文常译作“肉色红菇”或“裸齿红菇”）
                    Suillus luteus（中文常称“黄油蘑”或“黏盖牛肝菌”）
                    Lactarius torminosus 是一种乳菇属（Lactarius）的蘑菇，常被称为“绒毛乳菇”或“羊毛乳菇”
                    Lactarius resimus 乳白乳菇
                    Tricholoma terreum，中文常称为灰骑士菇或灰色骑士菇
                    Lactarius deterrimus（中文常译为“云杉乳菇”）
                    Sparassis crispa，中文常称“皱褶花菜菌”或“松茸花菜菌”
                    Craterellus cornucopioides，中文常称为“黑喇叭菌”或“丰收之角”
                    Amanita caesarea（中文常译作“凯撒鹅膏”或“凯撒菌”）
                    Marasmius oreades（中文常译作“仙环伞”或“草地小皮伞”）
                    Clitocybe nebularis（中文常译作“云灰漏斗伞”或“云状漏斗菌”
                    Wood blewit  别名：紫菇、蓝脚菌、Blewit
                    Pied de mouton（学名 Hydnum repandum），中文常译作“刺蘑”或“羊肚菌蘑”
                    St. George's mushroom 圣乔治蘑菇（Calocybe gambosa）是一种可食用的春季真菌，属于白蘑科 几乎不存在亚洲市场
                    Parasol mushroom 伞菌（学名 Macrolepiota procera）是一种大型可食用蘑菇或者应该被称为巨伞菌
                    Craterellus tubaeformis（中文常称“黄褐刺蘑”或“喇叭菌”）
                    Lactarius deliciosus 是一种属于乳菇属（Lactarius）的可食用真菌，常被称为“松乳菇”或“红乳牛肝菌”
                    Suillus spp. 指的是牛肝菌科的一属真菌，常被称为“滑菇”或“油头菇”
                    Leccinum scabrum 桦树林牛肝菌
                    
                    

                    美洲：
                    “Chicken of the woods”（中文常称“硫磺菌”或“硫磺多孔菌”）
                    舞茸（Hen of the Woods / Maitake）
                    Agaricus blazei是一种原产于巴西东南部山区的伞菌类真菌，隶属蘑菇科。因据称具有保健与药理作用，被称为“巴西蘑菇”或“姬松茸”
                    Pleurotus djamor（中文名：粉牡蛎菇、火烈鸟菇）

                    非洲：
                    Afroboletus 是一个非洲产的牛肝菌属真菌，隶属于牛肝菌科 非洲牛肝菌


                    云南：
                    鸡枞菌
                    松茸
                    牛肝菌
                    见手青
                    干巴菌
                    青头菌

                    东北：
                    榛蘑
                    褐牛肝菌

                    全球通用商业种
                    平菇
                    杏鲍菇
                    金针菇
                    白蘑菇
                    黑木耳

                    商业种增长中：
                    猴头菇
                    茶树菇
                    蟹味菇
                    竹荪
                    */

                    output.accept(ModItems.STRAW_MUSHROOM_COLONY.get());
                    output.accept(ModItems.SEA_MUSHROOM_COLONY.get());
                    output.accept(ModItems.CAVE_MUSHROOM_COLONY.get());
                    //output.accept(ModItems..get());       //不明GTFO种子
                    //output.accept(ModItems..get()); 
                    })
                    .build()
    );


    // 杂项
    public static final RegistryObject<CreativeModeTab> MANMADETAB = CREATIVE_TABS.register("other", () ->
            CreativeModeTab.builder()
                    .title(REGISTRATE.addLang("itemGroup", ListMod.rl("other"), "other"))  // 语言键: itemGroup.listmod.crops
                    .icon(() -> new ItemStack(ModItems.RED_ONION.get()))
                    .displayItems((parameters, output) -> {
                        //output.accept(ModItems..get());       //粗制花瓣饲料
                        //output.accept(ModItems..get());       //粗制瓜藤饲料
                        //output.accept(ModItems..get());       //粗制茄叶饲料
                        output.accept(ModItems.PURE_PUMKIN_FEED.get());       //纯净南瓜饲料
                        //output.accept(ModItems..get());       //纯净萝卜饲料
                        output.accept(ModItems.PURE_TOMATO_FEED.get());       //纯净番茄饲料
                        output.accept(ModItems.ADVANCED_PURE_MELON_MELON_FEED.get());       //高级瓜瓜纯净饲料
                        //output.accept(ModItems..get());       //高级84西瓜纯净饲料
                        output.accept(ModItems.ADVANCED_PURE_PEACH_FEED.get());       //高级桃桃纯净饲料
                        output.accept(ModItems.ADVANCED_PURE_FRUIT_FEED.get());       //高级水果纯净饲料
                        //output.accept(ModItems..get());       //高级蔬菜纯净饲料
                        //output.accept(ModItems..get());       //粗制腐肉饲料
                        output.accept(ModItems.PURE_BEEF_FEED.get());       //纯净牛肉饲料
                        output.accept(ModItems.PURE_PORKCHOP_FEED.get());       //纯净猪肉饲料
                        output.accept(ModItems.ADVANCED_PURE_MEAT_FEED.get());       //高级肉类饲料
                        output.accept(ModItems.PEARL_DUST.get());
                        output.accept(ModItems.WOODEN_SAND_FIXATION_GRID.get());       //木制固沙方格
                        output.accept(ModItems.WINE_VIAL.get());
                        output.accept(ModItems.WINE_BOTTLE.get());
                        output.accept(ModItems.EMPTY_CORNucOPIA.get());
                        output.accept(ModItems.PAN.get());
                        output.accept(ModItems.CAN.get()); 
                        //output.accept(ModItems..get());
                        output.accept(ModItems.SYALS_BAG.get()); 
                        output.accept(ModItems.CHIPS_BAG.get());
                        output.accept(ModItems.NAQUADAH_INGOT.get());
                        output.accept(ModItems.TUNGSTEN_STEEL.get()); 
                        output.accept(ModItems.ALUMINUM_INGOT.get()); 
                        output.accept(ModItems.ALUMINUM_FOIL.get()); 
                        output.accept(ModItems.TREE_SAP.get());
                        output.accept(ModItems.RAW_LACQUER_BUCKET.get());
                        output.accept(ModItems.URUSHI_BUCKET.get());
                        output.accept(ModItems.AZURE_NEODYMIUM_KNIFE.get());      
                        output.accept(ModItems.SCARLET_NEODYMIUM_KNIFE.get());      
                        output.accept(ModItems.CLEAVER_OF_HEAVY_BONE.get());      
                        output.accept(ModBlocks.CURING_SAND.get());
                        output.accept(ModBlocks.CURING_RED_SAND.get());
                        output.accept(ModBlocks.CURING_GRAVEL.get());
                        //output.accept(ModItems..get());       //
                        output.accept(ModBlocks.BURROWED_SAND.get());
                        output.accept(ModBlocks.BURROWED_GRAVEL.get());
                        output.accept(ModBlocks.BURROWED_CLAY.get());
                        output.accept(ModBlocks.BURROWED_DIRT.get());
                        output.accept(ModBlocks.BURROWED_MUD.get());
                        output.accept(ModBlocks.WATERMELON_BLOCK_SIDE.get());
                        output.accept(ModBlocks.WATERMELON_BLOCK_CORNER.get());
                        output.accept(ModBlocks.WATERMELON_FLESH_BLOCK.get());
                        //output.accept(ModItems..get()); 
                        //output.accept(ModItems..get()); 

                        output.accept(ModBlocks.PECAN_BAG.get());
                        output.accept(ModBlocks.ALLIUM_CRATE.get());       //囤积
                        output.accept(ModBlocks.AZURE_BLUET_CRATE.get());
                        output.accept(ModBlocks.BAKED_POTATO_CRATE.get());
                        output.accept(ModBlocks.BEEF_CRATE.get());
                        output.accept(ModBlocks.BEETROOT_CRATE.get());
                        output.accept(ModBlocks.BEETROOT_SEEDS_BAG.get());
                        output.accept(ModBlocks.BLACK_DYE_BUCKET.get());
                        output.accept(ModBlocks.BLAZE_POWDER_BAG.get());
                        output.accept(ModBlocks.BLAZE_ROD_BUNDLE.get());
                        output.accept(ModBlocks.BLUE_DYE_BUCKET.get());
                        output.accept(ModBlocks.BULE_ORCHID_CRATE.get());
                        output.accept(ModBlocks.BONE_PILE.get());
                        output.accept(ModBlocks.BOOK_PILE.get());
                        output.accept(ModBlocks.BOTTLE_RACK.get());
                        output.accept(ModBlocks.BREAD_CRATE.get());
                        output.accept(ModBlocks.BROWM_DYE_BUCKET.get());
                        output.accept(ModBlocks.CACTUS_BUNDLE.get());
                        output.accept(ModBlocks.CARROT_CRATE.get());
                        output.accept(ModBlocks.CHICKEN_CRATE.get());
                        output.accept(ModBlocks.CHORUS_FRUIT_CRATE.get());
                        output.accept(ModBlocks.COCOA_BEANS_BAG.get());
                        output.accept(ModBlocks.COD_BARREL.get());
                        output.accept(ModBlocks.COOKED_BEEF_CRATE.get());
                        output.accept(ModBlocks.COOKED_CHICKEN_CRATE.get());
                        output.accept(ModBlocks.COOKED_COD_BARREL.get());
                        output.accept(ModBlocks.COOKED_MUTTON_CRATE.get());
                        output.accept(ModBlocks.COOKED_RABBIT_CRATE.get());
                        output.accept(ModBlocks.COOKED_SALMON_BARREL.get());
                        output.accept(ModBlocks.COOKIE_CRATE.get());
                        output.accept(ModBlocks.CORNFLOWER_CRATE.get());
                        output.accept(ModBlocks.CRIMSON_FUNGUS_CRATE.get());
                        output.accept(ModBlocks.CYAN_DYE_BUCKET.get());
                        //output.accept(ModBlocks..get());
                        //output.accept(ModBlocks..get());
                        //output.accept(ModBlocks..get());
                        //output.accept(ModBlocks..get());
                        //output.accept(ModBlocks..get());
                        //output.accept(ModBlocks..get());
                        //output.accept(ModBlocks..get());
                        //output.accept(ModBlocks..get());
                        //output.accept(ModBlocks..get());
                        //output.accept(ModBlocks..get());
                        //output.accept(ModBlocks..get());
                        //output.accept(ModBlocks..get());
                        //output.accept(ModBlocks..get());
                        //output.accept(ModBlocks..get());
                        //output.accept(ModBlocks..get());
                        //output.accept(ModBlocks..get());
                        //output.accept(ModBlocks..get());
                        //output.accept(ModBlocks..get());
                        //output.accept(ModBlocks..get());
                        //output.accept(ModBlocks..get());
                        //output.accept(ModBlocks..get());
                        //output.accept(ModBlocks..get());
                        //output.accept(ModBlocks..get());
                        //output.accept(ModBlocks..get());
                        //output.accept(ModBlocks..get());
                        //output.accept(ModBlocks..get());
                        //output.accept(ModBlocks..get());
                        //output.accept(ModBlocks..get());
                        //output.accept(ModBlocks..get());
                        //output.accept(ModBlocks..get());
                        //output.accept(ModBlocks..get());
                        //output.accept(ModBlocks..get());


                    })
                    .build()
    );


    public static void register(IEventBus eventBus) {
        CREATIVE_TABS.register(eventBus);
    }
}

