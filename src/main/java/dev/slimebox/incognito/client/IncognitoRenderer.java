package dev.slimebox.incognito.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.slimebox.incognito.Incognito;
import dev.slimebox.incognito.IncognitoState;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraftforge.client.gui.ForgeIngameGui;

/**
 * Handles rendering things like the streaming overlay.
 *
 * @author Curle
 */
public class IncognitoRenderer {
    public static void renderOverlay(ForgeIngameGui gui, PoseStack mStack, float partialTicks, int width, int height) {
        if (IncognitoState.ENABLED) {
            mStack.pushPose();
            RenderSystem.setShaderTexture(0, Incognito.byMod("textures/gui/streaming.png"));
            GuiComponent.blit(mStack, width -32, 0, 0, 0, 32, 32, 32, 32);
            mStack.popPose();
        }
    }
}
