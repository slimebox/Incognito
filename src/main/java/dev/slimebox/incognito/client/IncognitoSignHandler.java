package dev.slimebox.incognito.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.LogicalSide;

import java.util.ArrayList;
import java.util.List;

/**
 * Incognito has special-casing for signs (and things that extend signs).
 * This way, if someone leaves a sign in front of an offline player's last position, their name is not shown to the audience.
 *
 * However, this leads to some pretty gross code.
 * All of that is contained here.
 *
 * @author Curle
 */
public class IncognitoSignHandler {

    public static void clientTick(TickEvent.ClientTickEvent event) {
        // Must be working on the render thread
        if (event.side != LogicalSide.CLIENT) return;
        // Must be in a world
        if (Minecraft.getInstance().player == null) return;

        List<SignBlockEntity> signs = new ArrayList<>();
        Level world = Minecraft.getInstance().level;

    }
}
