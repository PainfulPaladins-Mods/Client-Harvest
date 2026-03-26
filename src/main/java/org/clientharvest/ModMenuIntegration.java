package org.clientharvest;

import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuIntegration implements ModMenuApi {
    YetAnotherConfigLib.createBuilder()
        .title(Component.literal("Used for narration. Could be used to render a title in the future."))
            .category(ConfigCategory.createBuilder()
                .name(Component.literal("Name of the category"))
            .tooltip(Component.literal("This text will appear as a tooltip when you hover or focus the button with Tab. There is no need to add \n to wrap as YACL will do it for you."))
            .group(OptionGroup.createBuilder()
                        .name(Component.literal("Name of the group"))
            .description(OptionDescription.of(Component.literal("This text will appear when you hover over the name or focus on the collapse button with Tab.")))
            .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Boolean Option"))
            .description(OptionDescription.of(Component.literal("This text will appear as a tooltip when you hover over the option.")))
            .binding(true, () -> this.myBooleanOption, newVal -> this.myBooleanOption = newVal)
            .controller(TickBoxControllerBuilder::create)
                                .build())
            .build())
            .build())
            .build()
}
