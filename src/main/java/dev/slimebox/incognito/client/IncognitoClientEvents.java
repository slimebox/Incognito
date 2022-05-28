package dev.slimebox.incognito.client;

import dev.slimebox.incognito.Incognito;
import dev.slimebox.incognito.rename.Renamer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderNameplateEvent;
import net.minecraftforge.client.gui.OverlayRegistry;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.util.ArrayList;
import java.util.List;

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
    public static void clientTick(TickEvent.ClientTickEvent event) {
        // Must be working on the render thread
        if (event.side != LogicalSide.CLIENT) return;
        // Must be in a world
        if (Minecraft.getInstance().player == null) return;

        ViewArea area = ObfuscationReflectionHelper.getPrivateValue(LevelRenderer.class, Minecraft.getInstance().levelRenderer, "f_1094" + "69_");

        // For every chunk in the renderable area..
        for (ChunkRenderDispatcher.RenderChunk chunk : area != null ? area.chunks : new ChunkRenderDispatcher.RenderChunk[0]) {
            ChunkRenderDispatcher.CompiledChunk compiled = chunk.getCompiledChunk();
            // Read all BlockEntities in range..
            for (BlockEntity entity : compiled.getRenderableBlockEntities()) {
                // Filter for signs...
                if (entity instanceof SignBlockEntity sign) {
                    // Remap all text
                    for (int i = 0; i < 4; i++)
                        sign.setMessage(i, Incognito.API.remapComponent(sign.getMessage(i, false)));
                }
            }
        }
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
