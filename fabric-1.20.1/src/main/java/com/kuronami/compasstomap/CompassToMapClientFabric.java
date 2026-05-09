package com.kuronami.compasstomap;

import net.fabricmc.api.ClientModInitializer;

/**
 * Fabric 1.20.1 client init.
 * v2.0 では JM 統合 disable のため payload receiver 登録なし (server side chat 通知のみ動作)。
 */
public class CompassToMapClientFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // No-op for v2.0 Fabric 1.20.1
    }
}
