package org.clientharvest;


import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.CocoaBlock;
import net.minecraft.block.CropBlock;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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



    public static int locateSlot(MinecraftClient client, Item item) {
        for (int i = 0; i < 9; i++) {
            ItemStack slot = client.player.getInventory().getStack(i);
            Item slotItem = slot.getItem();
            if (item == slotItem) return i;
        }
        return -1;
    }

    public static ActionResult switchToItem(MinecraftClient client, Item item){
        int slot = locateSlot(client,item);
        if (slot == -1) return ActionResult.FAIL;
        if (client.player.getInventory().selectedSlot == slot) return ActionResult.PASS;
        client.player.getInventory().selectedSlot = slot;
        System.out.println(slot);

        return ActionResult.SUCCESS;
    }

    public ActionResult onBlockUse(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        BlockState state = world.getBlockState(hitResult.getBlockPos());
        int lastSlot = client.player.getInventory().selectedSlot;
        if (!isMature(state)) return ActionResult.PASS;
        if (switchToItem(client,state.getBlock().asItem()) == ActionResult.SUCCESS) client.tick();
        ClientPlayerInteractionManager interaction = client.interactionManager;
        interaction.attackBlock(hitResult.getBlockPos(),hitResult.getSide());

        client.player.getInventory().selectedSlot = lastSlot;
        return ActionResult.SUCCESS;
    }
}