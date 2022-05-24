package dev.slimebox.incognito.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.slimebox.incognito.IncognitoState;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;

public class IncognitoConfigScreen extends Screen {
    private CycleButton<Boolean> enableButton;
    private Screen lastScreen;

    public IncognitoConfigScreen(Screen lastScreen) {
        super(new TranslatableComponent("incognito.gui.config"));
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        super.init();
        this.enableButton = this.addRenderableWidget(CycleButton.booleanBuilder(new TranslatableComponent("incognito.gui.enable"), new TranslatableComponent("incognito.gui.disable"))
                .withInitialValue(true)
                .create(this.width / 2 - 100, this.height / 4 + 96 + 18, 200, 20, new TranslatableComponent("incognito.gui.toggle"), (button, value) -> {
                    IncognitoState.ENABLED = !IncognitoState.ENABLED;
                })
        );
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
        // draw status on screen ("incognito.state.current")
        drawCenteredString(stack, this.font, new TranslatableComponent("incognito.state.current",
                IncognitoState.ENABLED ? new TranslatableComponent("incognito.state.enabled") : new TranslatableComponent("incognito.state.disabled")),
                this.width / 2, 45, 16777215);

        this.enableButton.render(stack, width, height, ratio);

        super.render(stack, width, height, ratio);

    }
}
