package org.clientharvest;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.CocoaBlock;
import net.minecraft.block.CropBlock;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import net.minecraft.text.Text;

import org.lwjgl.glfw.GLFW;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class HarvestModClient implements ClientModInitializer {
    public static MinecraftClient client;
    public static boolean enabled = true;
    private static KeyBinding toggleKey;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File("config/clientharvest.json");
    public static class Config {
        public boolean enabled = true;
    }

    public final KeyBinding.Category category = KeyBinding.Category.create(Identifier.of("categories", "clientharvest"));

    @Override
    public void onInitializeClient() {
        client = MinecraftClient.getInstance();
        loadConfig();
        UseBlockCallback.EVENT.register(this::onBlockUse);
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.clientharvest.toggle",
                GLFW.GLFW_KEY_G,
                category
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                enabled = !enabled;
                saveConfig();
                if (client.player != null) {
                    client.inGameHud.setOverlayMessage(
                            Text.literal("[Client Harvest] " + (enabled ? "ON" : "OFF")),
                            false
                    );
                }
            }
        });
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
        if (!enabled) return ActionResult.PASS;
        if (client.interactionManager == null) return ActionResult.PASS;
        BlockState state = world.getBlockState(hitResult.getBlockPos());
        if (!isMature(state)) return ActionResult.PASS;
        client.interactionManager.attackBlock(
                hitResult.getBlockPos(),
                hitResult.getSide()
        );
        return ActionResult.SUCCESS;
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