package com.kuronami.compasstomap.event;

import com.kuronami.compasstomap.CompassToMapFabric;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public final class ClientDispatch {
    public static final ResourceLocation STRUCTURE = new ResourceLocation(CompassToMapFabric.MODID, "structure_found");
    public static final ResourceLocation BIOME = new ResourceLocation(CompassToMapFabric.MODID, "biome_found");

    private ClientDispatch() {}

    static void sendStructure(ServerPlayer player, String id, BlockPos pos, ResourceKey<Level> dimension) {
        send(player, STRUCTURE, id, pos, dimension);
    }

    static void sendBiome(ServerPlayer player, String id, BlockPos pos, ResourceKey<Level> dimension) {
        send(player, BIOME, id, pos, dimension);
    }

    private static void send(ServerPlayer player, ResourceLocation channel, String id,
                             BlockPos pos, ResourceKey<Level> dimension) {
        if (!ServerPlayNetworking.canSend(player, channel)) return;
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeUtf(id);
        buf.writeBlockPos(pos);
        buf.writeResourceLocation(dimension.location());
        ServerPlayNetworking.send(player, channel, buf);
    }
}
