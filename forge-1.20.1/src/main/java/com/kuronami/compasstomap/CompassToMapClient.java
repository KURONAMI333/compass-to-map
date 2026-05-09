package com.kuronami.compasstomap;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;

/**
 * クライアント限定の初期化 (Forge 1.21.1)。
 *
 * 現状はクライアント側の特別な初期化は無し。Config は TOML ファイル直編集で対応。
 * 将来的に Mods 画面の Config GUI を有効化する場合はここに追加。
 *
 * 注: Forge 1.21.1 では ConfigScreenHandler API のクラス位置が安定しないため、
 *     in-game GUI は v2.0 では未対応 (config/compasstomap-common.toml を直接編集)。
 */
@Mod.EventBusSubscriber(modid = CompassToMap.MODID, value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.MOD)
public final class CompassToMapClient {
    private CompassToMapClient() {}
}
