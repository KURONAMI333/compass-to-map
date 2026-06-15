package com.kuronami.compasstomap.compat.jm;

import com.kuronami.compasstomap.CompassToMapFabric;

import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.client.IClientPlugin;
import journeymap.api.v2.common.JourneyMapPlugin;

/**
 * JourneyMap が起動時に検出して呼び出すプラグインエントリ。
 * IClientAPI ハンドルを保持し、後で waypoint 登録に使う。
 */
@JourneyMapPlugin(apiVersion = IClientAPI.API_VERSION)
public class CompassToMapJourneyMapPlugin implements IClientPlugin {

    public static volatile IClientAPI api;

    @Override
    public void initialize(IClientAPI jmClientApi) {
        api = jmClientApi;
        CompassToMapFabric.LOGGER.info("JourneyMap API initialized for Compass to Map");
    }

    @Override
    public String getModId() {
        return CompassToMapFabric.MODID;
    }
}
