package com.kuronami.compasstomap.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * 構造物発見時 server→client 用 payload (Forge 1.20.1 SimpleChannel)。
 * 1.21.1 と同じ手書き encode/decode (CustomPacketPayload は 1.20.5+)。
 */
public record StructureFoundPayload(String structureId, BlockPos pos, ResourceKey<Level> dimension) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(structureId);
        buf.writeBlockPos(pos);
        buf.writeResourceKey(dimension);
    }

    public static StructureFoundPayload decode(FriendlyByteBuf buf) {
        String id = buf.readUtf();
        BlockPos p = buf.readBlockPos();
        ResourceKey<Level> dim = buf.readResourceKey(net.minecraft.core.registries.Registries.DIMENSION);
        return new StructureFoundPayload(id, p, dim);
    }
}
