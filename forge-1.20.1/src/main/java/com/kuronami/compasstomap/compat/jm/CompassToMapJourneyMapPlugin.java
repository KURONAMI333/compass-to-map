package com.kuronami.compasstomap.compat.jm;

import com.kuronami.compasstomap.CompassToMap;

import journeymap.client.api.ClientPlugin;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.IClientPlugin;
import journeymap.client.api.event.ClientEvent;

/**
 * JourneyMap 1.20.1 plugin entry (v1 API).
 * 1.21+ の journeymap.api.v2.common.JourneyMapPlugin とは別 annotation/package.
 */
@ClientPlugin
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

    @Override
    public void onEvent(ClientEvent event) {
        // 未使用
    }
}
