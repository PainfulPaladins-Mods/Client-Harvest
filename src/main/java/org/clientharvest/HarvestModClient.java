package org.clientharvest;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import org.clientharvest.ClientHarvestConfig;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import org.lwjgl.glfw.GLFW;
import java.io.File;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class HarvestModClient implements ClientModInitializer {
    public static Minecraft client;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File("config/clientharvest.json");

    private int prevslot = 0;
    private BlockPos pendingPos = null;
    private Direction pendingFace = null;
    public final KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("clientharvest", "general"));

    @Override
    public void onInitializeClient() {
        client = Minecraft.getInstance();

        UseBlockCallback.EVENT.register(this::onBlockUse);
        KeyMapping toggleKey = (KeyMapping) KeyMappingHelper.registerKeyMapping(
                new KeyMapping(
                        "key.clientharvest.toggle",
                        GLFW.GLFW_KEY_G,
                        category
                )
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.consumeClick()) {
                ClientHarvestConfig.YaclConfig config = ClientHarvestConfig.ConfigManager.getConfig();
                config.Enabled = !config.Enabled;
                ClientHarvestConfig.ConfigManager.save();
                if (client.player != null) {
                    client.gui.setOverlayMessage(
                            Component.literal("[Client Harvest] " + (ClientHarvestConfig.ConfigManager.getConfig().Enabled ? "ON" : "OFF")),
                            false
                    );
                }
            }
            clientTick();
        });
    }

    private static boolean isMature(BlockState state) {
        if (state.getBlock() instanceof CocoaBlock) {
            return state.getValue(CocoaBlock.AGE) >= CocoaBlock.MAX_AGE;
        } else if (state.getBlock() instanceof CropBlock cropBlock) {
            return cropBlock.isMaxAge(state);
        } else if (state.getBlock() instanceof NetherWartBlock) {
            return state.getValue(NetherWartBlock.AGE) >= 3;
        }
        return false;
    }

    public void clientTick() {
        if (pendingPos == null) return;
        if (client.gameMode == null || client.player == null) return;
        BlockHitResult placeHit = new BlockHitResult(
                Vec3.atCenterOf(pendingPos),
                pendingFace,
                pendingPos,
                false
        );
        client.gameMode.useItemOn(
                client.player,
                InteractionHand.MAIN_HAND,
                placeHit
        );
        pendingPos = null;
        if (ClientHarvestConfig.ConfigManager.getConfig().BackSlot) {
            client.player.getInventory().setSelectedSlot(prevslot);
        };
    }

    public InteractionResult onBlockUse(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide()) return InteractionResult.PASS;
        if (!ClientHarvestConfig.ConfigManager.getConfig().Enabled) return InteractionResult.PASS;
        if (client.gameMode == null) return InteractionResult.PASS;

        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        BlockPos pos = hitResult.getBlockPos();
        BlockState state = level.getBlockState(pos);
        if (!isMature(state)) return InteractionResult.PASS;

        prevslot = player.getInventory().getSelectedSlot();
        ItemStack placeItem = new ItemStack(state.getBlock().asItem());
        if (player.getMainHandItem().getItem() != placeItem.getItem()) {
            for (int i = 0; i < 9; i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (!stack.isEmpty() && stack.getItem() == placeItem.getItem()) {
                    player.getInventory().setSelectedSlot(i);
                    break;
                }
            }
        }

        client.gameMode.startDestroyBlock(pos, hitResult.getDirection());
        pendingPos = pos;
        pendingFace = hitResult.getDirection();
        return InteractionResult.CONSUME;
    }
}