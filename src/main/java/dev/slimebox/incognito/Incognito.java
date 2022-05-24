package dev.slimebox.incognito;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.slimebox.incognito.api.IncognitoAPI;
import dev.slimebox.incognito.client.IncognitoClientEvents;
import dev.slimebox.incognito.client.IncognitoConfigScreen;
import dev.slimebox.incognito.client.IncognitoRenderer;
import dev.slimebox.incognito.rename.Renamer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.OverlayRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * The Going Incognto main class.
 *
 * Going Incognito allows you to hide the identity of everyone else on a server.
 * This allows avoiding things like stream sniping, or merely hiding the server you're on.
 *
 * To do this, it creates a map of username to pseudonym. Every time a username is about to
 *  be rendered, it consults this map.
 *
 * The map is consistent across sessions (such that the same user always has the same pseudonym)
 * The map can be viewed and modified in-game by the player.
 *
 * When the mod is enabled, a status indicator shows on-screen, and all changed names will be highlighted blue.
 *
 * The current list of places this mod can touch:
 *  - Tab user list
 *  - Nameplates
 *  - Chat messages (bidirectionally, so you can type someone's changed name and they receive their actual name)
 *
 * The following places are WIP:
 *  - Item tooltips
 *  - Books
 *  - Signs
 *
 * The mod cannot adjust things like in-game overlays.
 * The mod cannot replace substrings of names - if you call someone by a shortened version of their replaced name, it can not be changed.
 *
 * @author Curle
 */
@Mod(Incognito.MODID)
public class Incognito {

    public static final String MODID = "incognito";
    public static ResourceLocation byMod(String name) { return new ResourceLocation(MODID, name); }

    public static IncognitoAPI API;

    public Incognito() {
        // Load the name pool
        IncognitoState.NAME_POOL = Lists.newArrayList("Narf", "cheezey_tiger", "AuspiciousFlammenwerfer");

        API = new Renamer();
    }
}
