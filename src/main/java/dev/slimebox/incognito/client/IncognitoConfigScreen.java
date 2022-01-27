package dev.slimebox.incognito.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.slimebox.incognito.IncognitoState;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;

public class IncognitoConfigScreen extends Screen {
    private Button enableButton;
    private Screen lastScreen;

    public IncognitoConfigScreen(Screen lastScreen) {
        super(new TranslatableComponent("incognito.gui.config"));
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        super.init();
        this.enableButton = this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 4 + 96 + 18, 200, 20, new TranslatableComponent("incognito.gui.enable"), (p_96030_) -> {
            this.enable();
        }));
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 4 + 120 + 18, 200, 20, CommonComponents.GUI_DONE, (p_169297_) -> {
            this.minecraft.setScreen(this.lastScreen);
        }));
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void render(PoseStack stack, int width, int height, float ratio) {
        renderBackground(stack); // draw dirt background
        // draw title on screen ("incognito.gui.config")
        drawCenteredString(stack, this.font, this.title, this.width / 2, 17, 16777215);

        this.enableButton.render(stack, width, height, ratio);

        super.render(stack, width, height, ratio);

    }

    private void enable() {
        IncognitoState.ENABLED = true;
    }
}
