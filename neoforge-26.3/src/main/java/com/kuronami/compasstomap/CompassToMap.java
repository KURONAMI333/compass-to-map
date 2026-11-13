package com.kuronami.compasstomap;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;

import org.slf4j.Logger;

/**
 * Compass to Map: ExplorersCompass × JourneyMap Addon
 *
 * ExplorersCompass で構造物を発見した瞬間、その座標を JourneyMap に
 * waypoint として自動登録する。
 *
 * 仕組み:
 *  - サーバ側 PlayerTickEvent.Post で各プレイヤーの inventory を走査
 *  - ExplorersCompass の DataComponent (COMPASS_STATE_COMPONENT 等) を監視
 *  - COMPASS_STATE = FOUND かつ前回値と異なれば「新規発見」と判定
 *  - クライアントへ payload 送信 → JourneyMap 上に waypoint 登録
 *
 * Mixin / リフレクション不要 (EC が public static フィールドで API 提供)
 */
@Mod(CompassToMap.MODID)
public class CompassToMap {

    public static final String MODID = "compasstomap";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CompassToMap(IEventBus modEventBus, ModContainer modContainer) {
        // ゲーム実行時イベント (PlayerTickEvent.Post 等)
        NeoForge.EVENT_BUS.register(com.kuronami.compasstomap.event.CompassWatcher.class);

        // Config 登録
        // 26.3 の fancymodloader 12 で COMMON は LOCAL に改名された (26.3_MIGRATION_NOTES.md)。
        // LOCAL の既定ファイル名は -local.toml になるので、26.2 以前と同じ -common.toml を明示する
        modContainer.registerConfig(ModConfig.Type.LOCAL, Config.SPEC, "compasstomap-common.toml");
    }
}
