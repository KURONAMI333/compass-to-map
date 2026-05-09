package com.kuronami.compasstomap.compat.jm;

import com.kuronami.compasstomap.CompassToMap;

import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.client.IClientPlugin;
import journeymap.api.v2.common.JourneyMapPlugin;

/**
 * JourneyMap が起動時に検出して呼び出すプラグインエントリ。
 * Forge / NeoForge 共通 (JM API は loader 非依存)。
 */
@JourneyMapPlugin(apiVersion = IClientAPI.API_VERSION)
public class CompassToMapJourneyMapPlugin implements IClientPlugin {

    public static volatile IClientAPI api;

    @Override
    public void initialize(IClientAPI jmClientApi) {
        api = jmClientApi;
        CompassToMap.LOGGER.info("JourneyMap API initialized for Compass to Map");
    }

    @Override
    public String getModId() {
        return CompassToMap.MODID;
    }
}
