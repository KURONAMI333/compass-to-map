package com.kuronami.compasstomap.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record BiomeFoundPayload(String biomeId, BlockPos pos, ResourceKey<Level> dimension) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(biomeId);
        buf.writeBlockPos(pos);
        buf.writeResourceKey(dimension);
    }

    public static BiomeFoundPayload decode(FriendlyByteBuf buf) {
        String id = buf.readUtf();
        BlockPos p = buf.readBlockPos();
        ResourceKey<Level> dim = buf.readResourceKey(net.minecraft.core.registries.Registries.DIMENSION);
        return new BiomeFoundPayload(id, p, dim);
    }
}
