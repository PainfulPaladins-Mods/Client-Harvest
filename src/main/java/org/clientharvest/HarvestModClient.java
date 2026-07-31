package org.clientharvest;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.ToggleKeyMapping;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import org.lwjgl.glfw.GLFW;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class HarvestModClient implements ClientModInitializer {
    public static Minecraft client;
    public static boolean enabled = true;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File("config/clientharvest.json");
    public static class Config {
        public boolean enabled = true;
    }

    public final ToggleKeyMapping.Category category = ToggleKeyMapping.Category.register(Identifier.fromNamespaceAndPath("clientharvest", "general"));

    @Override
    public void onInitializeClient() {
        client = Minecraft.getInstance();
        loadConfig();
        UseBlockCallback.EVENT.register(this::onBlockUse);
        KeyMapping toggleKey = KeyMappingHelper.registerKeyMapping(
                new KeyMapping(
                        "key.clientharvest.toggle",
                        GLFW.GLFW_KEY_G,
                        category
                )
        );
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.consumeClick()) {
                enabled = !enabled;
                saveConfig();
                if (client.player != null) {
                    client.gui.hud.setOverlayMessage(
                            Component.literal("[Client Harvest] " + (enabled ? "ON" : "OFF")),
                            false
                    );
                }
            }
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

    public InteractionResult onBlockUse(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if (!enabled) return InteractionResult.PASS;
        if (client.gameMode == null) return InteractionResult.PASS;
        BlockState state = level.getBlockState(hitResult.getBlockPos());
        if (!isMature(state)) return InteractionResult.PASS;
        client.gameMode.startDestroyBlock(
                hitResult.getBlockPos(),
                hitResult.getDirection()
        );
        return InteractionResult.SUCCESS;
    }

    private static void loadConfig() {
        try {
            if (!CONFIG_FILE.exists()) {
                saveConfig();
                return;
            }
            FileReader reader = new FileReader(CONFIG_FILE);
            Config config = GSON.fromJson(reader, Config.class);
            reader.close();
            if (config != null) {
                enabled = config.enabled;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveConfig() {
        try {
            CONFIG_FILE.getParentFile().mkdirs();
            Config config = new Config();
            config.enabled = enabled;
            FileWriter writer = new FileWriter(CONFIG_FILE);
            GSON.toJson(config, writer);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
