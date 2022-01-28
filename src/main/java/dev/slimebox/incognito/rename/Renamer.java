package dev.slimebox.incognito.rename;

import dev.slimebox.incognito.Incognito;
import dev.slimebox.incognito.IncognitoState;
import dev.slimebox.incognito.api.IncognitoAPI;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderNameplateEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;


/**
 * Handles the allocation and fetching of user map renames.
 * Usernames are string-based, not UUID based.
 * Thus, a user who changes username will be allocated a new entry in the map.
 *
 * @author Curle
 */
public final class Renamer implements IncognitoAPI {
    static private Collection<PlayerInfo> userListCache;

    /**
     * Fetch (and allocate, if it doesn't already exist) a name for the given player.
     */
    @Override
    public String remapPlayer(PlayerInfo player) {
        return remapPlayerName(player.getProfile().getName());
    }

    /**
     * Map the given username to an alias.
     * Remapped usernames are consistent across restarts, but they are based on the value of the string.
     * Thus, a player who changes username will get a fresh mapped name.
     * @param playerName the username of the player to rename.
     * @return a new name to be used instead of their actual name.
     */
    @Override
    public String remapPlayerName(String playerName) {
        if (IncognitoState.NAME_MAP.containsKey(playerName)) {
            return IncognitoState.NAME_MAP.get(playerName);
        } else {
            String newName = IncognitoState.NAME_POOL.get(0);
            IncognitoState.NAME_MAP.put(playerName, newName);
            IncognitoState.NAME_POOL.remove(newName);
            return newName;
        }
    }

    @Override
    public void addPlayerNameToPool(String name) {
        IncognitoState.NAME_POOL.add(name);
    }

    @Override
    public String remapText(String text) {
        for (PlayerInfo player : userListCache) {
            String playerName = player.getProfile().getName();
            if (text.toLowerCase(Locale.ROOT).contains(playerName.toLowerCase(Locale.ROOT))) {
                text = text.replaceAll(playerName, Incognito.API.remapPlayerName(playerName));
            }
        }

        return text;
    }

    @Override
    public Component remapComponent(Component input) {
        // Remap siblings recursively
        input.getSiblings().forEach(this::remapComponent);

        // If there's a text component, copy and edit
        // TODO: this will lose all prior formatting
        if (input instanceof TextComponent text) {
            for (PlayerInfo player : userListCache) {
                String msg = text.getText(), name = player.getProfile().getName();
                if (msg.contains(name)) {
                    Component remapped = new TextComponent(
                            msg.substring(0, msg.indexOf(name))
                    ).append(
                            new TextComponent(
                                    remapPlayer(player)).withStyle(ChatFormatting.AQUA)
                    ).append(
                            new TextComponent(
                                    msg.substring(msg.indexOf(name) + name.length())
                            )
                    );

                    remapped.getSiblings().addAll(text.getSiblings());
                }
            }
        }
        if (input instanceof TranslatableComponent translatable) {
            // parts: "<", name, ">", message
            List<FormattedText> parts = ObfuscationReflectionHelper.getPrivateValue(TranslatableComponent.class, translatable, "f_131301" + "_");
            List<FormattedText> newParts = new ArrayList<>(parts);

            // Need to index by number, so use a traditional loop..
            for (int i = 0; i < newParts.size(); i++ ) {
                FormattedText text = newParts.get(i);
                if (text instanceof Component component) {
                    newParts.set(i, remapComponent(component));
                }
            }
            ObfuscationReflectionHelper.setPrivateValue(TranslatableComponent.class, translatable, newParts, "f_131301" + "_");

            return translatable;
            /*
            // Rename player
            messageAuthorName = Incognito.API.remapPlayerName(messageAuthorName);

            // Save player name
            Component playerName = ((TextComponent) args[0]).getSiblings().get(0);
            TextComponent newMessage = new TextComponent(messageAuthorName);
            newMessage.withStyle(playerName.getStyle());
            newMessage.withStyle(ChatFormatting.AQUA);

            ((TextComponent) args[0]).getSiblings().set(0, newMessage);
            */

        }

        return input;
    }

    /**
     * Refresh the tab user list.
     * This must be called after the user list is synchronized.
     * This must be called after the user list changes.
     */
    public static void renameUserList() {
        if (!IncognitoState.ENABLED) return;
        ClientPacketListener clientpacketlistener = Minecraft.getInstance().player.connection;
        userListCache = clientpacketlistener.getOnlinePlayers();

        userListCache.forEach(player -> {
            player.setTabListDisplayName(new TextComponent(Incognito.API.remapPlayer(player)).withStyle(ChatFormatting.BLUE));
        });
    }

    /**
     * Given an incoming chat message, mutate it to replace user names.
     * @param event the ClientChatReceivedEvent instance that signals an incoming message.
     */
    public static void renameChatMessage(ClientChatReceivedEvent event) {
        if (!IncognitoState.ENABLED) return;

        event.setMessage(Incognito.API.remapComponent(event.getMessage()));
    }

    /**
     * Given an entity nameplate attempting to render, mutate it to replace the username.
     * @param event the RenderNameplateEvent instance that signals a nameplate render.
     */
    public static void renameNamePlate(RenderNameplateEvent event) {
        if (!IncognitoState.ENABLED) return;

        event.setContent(Incognito.API.remapComponent(event.getContent()));
    }

}
