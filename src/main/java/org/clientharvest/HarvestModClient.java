package org.clientharvest;


import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.CocoaBlock;
import net.minecraft.block.CropBlock;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;


public class HarvestModClient implements ClientModInitializer {
    public static MinecraftClient client;
    @Override
    public void onInitializeClient() {
        client = MinecraftClient.getInstance();
        UseBlockCallback.EVENT.register(this::onBlockUse);
    }

    private static boolean isMature(BlockState state) {
        if (state.getBlock() instanceof CocoaBlock) {
            return state.get(CocoaBlock.AGE) >= CocoaBlock.MAX_AGE;
        } else if (state.getBlock() instanceof CropBlock cropBlock) {
            return cropBlock.isMature(state);
        } else if (state.getBlock() instanceof NetherWartBlock) {
            return state.get(NetherWartBlock.AGE) >= 3;
        }

        return false;
    }
    public ActionResult onBlockUse(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        BlockState state = world.getBlockState(hitResult.getBlockPos());
        if (!isMature(state)) return ActionResult.PASS;
        ClientPlayerInteractionManager interaction = new ClientPlayerInteractionManager(client, client.getNetworkHandler());
        interaction.attackBlock(hitResult.getBlockPos(),hitResult.getSide());
        return ActionResult.SUCCESS;
    }
}