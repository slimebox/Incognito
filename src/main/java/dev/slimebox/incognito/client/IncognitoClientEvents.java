package dev.slimebox.incognito.client;

import dev.slimebox.incognito.Incognito;
import dev.slimebox.incognito.rename.Renamer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderNameplateEvent;
import net.minecraftforge.client.gui.OverlayRegistry;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE, modid = Incognito.MODID)
public class IncognitoClientEvents {

    @SubscribeEvent
    public static void onChat(ClientChatReceivedEvent event) {
        Renamer.renameChatMessage(event);
    }

    @SubscribeEvent
    public static void onUserList(RenderGameOverlayEvent.Pre event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.PLAYER_LIST)
            Renamer.renameUserList();
    }

    @SubscribeEvent
    public static void onNameplate(RenderNameplateEvent event) {
        Renamer.renameNamePlate(event);
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = Incognito.MODID)
    static class IncognitoModClientEvents {

        @SubscribeEvent
        public static void clientSetup(FMLClientSetupEvent event) {
            OverlayRegistry.registerOverlayTop("incognito_streaming", IncognitoRenderer::renderOverlay);
            ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class, () ->
                    new ConfigGuiHandler.ConfigGuiFactory(((minecraft, screen) -> new IncognitoConfigScreen(screen)))
            );
        }

    }

}
