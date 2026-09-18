package com.kuronami.compasstomap.compat.jm;

import com.kuronami.compasstomap.CompassToMapFabric;
import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.client.IClientPlugin;
import journeymap.api.v2.common.JourneyMapPlugin;

@JourneyMapPlugin(apiVersion = IClientAPI.API_VERSION)
public final class CompassToMapJourneyMapPlugin implements IClientPlugin {
    public static volatile IClientAPI api;

    @Override
    public void initialize(IClientAPI clientApi) {
        api = clientApi;
        CompassToMapFabric.LOGGER.info("JourneyMap API initialized for Compass to Map");
    }

    @Override
    public String getModId() {
        return CompassToMapFabric.MODID;
    }
}
