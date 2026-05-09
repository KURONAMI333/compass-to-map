package com.kuronami.compasstomap.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Nature's Compass によるバイオーム発見時にサーバ→クライアントへ送信される
 * waypoint 登録依頼 (v2.0 で追加)。Forge 1.21.1 SimpleChannel 用。
 *
 * @param biomeId       バイオーム ID (例: minecraft:desert)
 * @param pos           推定座標 (Y は Heightmap or fallback で補完済み)
 * @param dimension     対象ディメンション
 */
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
