package dev.slimebox.incognito;

import com.google.common.collect.Lists;
import dev.slimebox.incognito.client.IncognitoConfigScreen;
import dev.slimebox.incognito.rename.Renamer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderNameplateEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;

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

    public Incognito() {
        ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class, () ->
                new ConfigGuiHandler.ConfigGuiFactory(((minecraft, screen) -> new IncognitoConfigScreen(screen)))
        );

        MinecraftForge.EVENT_BUS.addListener(this::onChat);
        MinecraftForge.EVENT_BUS.addListener(this::onLoad);
        MinecraftForge.EVENT_BUS.addListener(this::onNameplate);

        // Load the name pool
        IncognitoState.NAME_POOL = Lists.newArrayList("Narf", "cheezey_tiger", "AuspiciousFlammenwerfer");

    }

    public void onChat(ClientChatReceivedEvent event) {
        Renamer.renameChatMessage(event);
    }

    public void onLoad(ClientPlayerNetworkEvent.LoggedInEvent event) {
        Renamer.renameUserList();
    }

    public void onNameplate(RenderNameplateEvent event) {
        Renamer.renameNamePlate(event);
    }
}
