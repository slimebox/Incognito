package dev.slimebox.incognito.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.slimebox.incognito.Incognito;
import dev.slimebox.incognito.IncognitoState;
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
            // x, y, u, v, w, h
            gui.blit(mStack, width - 16, 0, 0, 0, 16, 16);
            mStack.popPose();
        }
    }
}
