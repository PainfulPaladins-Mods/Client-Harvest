package org.clientharvest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.nio.file.Files;
import java.nio.file.Path;

public class ClientHarvestConfig {
    public static class YaclConfig {
        public boolean Enabled = true;
        public boolean BackSlot = true;
    }

    public static class ConfigManager {
        private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
        private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("elytrahud.json");
        private static YaclConfig _config = null;

        public static YaclConfig getConfig() {
            if (_config == null) {
                _config = load();
            }
            return _config;
        }

        public static void save() {
            try {
                Files.writeString(CONFIG_PATH, GSON.toJson(getConfig()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private static YaclConfig load() {
            try {
                if (Files.exists(CONFIG_PATH)) {
                    return GSON.fromJson(Files.readString(CONFIG_PATH), YaclConfig.class);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new YaclConfig();
        }
    }

    public static class ConfigScreen {
        public static Screen createConfigScreen(Screen parent) {
            YaclConfig config = ConfigManager.getConfig();
            return YetAnotherConfigLib.createBuilder()
                    .title(Component.literal("Client Harvest Settings"))
                    .save(ConfigManager::save)
                    .category(ConfigCategory.createBuilder()
                            .name(Component.literal("General"))
                            .option(createBooleanOption("Mod Enabled", "Enable or disable the entire mod",
                                    () -> config.Enabled, v -> config.Enabled = v))
                            .option(createBooleanOption("Back Slot", "Whether or not to return the slot",
                                    () -> config.BackSlot, v -> config.BackSlot = v))
                            .build())
                    .build()
                    .generateScreen(parent);
        }

        private static Option<Boolean> createBooleanOption(String name, String description,
                                                           java.util.function.Supplier<Boolean> getter,
                                                           java.util.function.Consumer<Boolean> setter) {
            return Option.<Boolean>createBuilder()
                    .name(Component.literal(name))
                    .description(OptionDescription.of(Component.literal(description)))
                    .binding(true, getter, setter)
                    .controller(opt -> BooleanControllerBuilder.create(opt).yesNoFormatter().coloured(true))
                    .listener((opt, value) -> { setter.accept(value); ConfigManager.save(); })
                    .build();
        }
    }
}
