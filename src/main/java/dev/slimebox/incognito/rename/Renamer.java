package dev.slimebox.incognito.rename;

import dev.slimebox.incognito.IncognitoState;
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
public final class Renamer {
    static private Collection<PlayerInfo> userListCache;

    /**
     * Fetch (and allocate, if it doesn't already exist) a name for the given player.
     */
    public static String tryRenamePlayer(PlayerInfo player) {
        return tryRenamePlayerName(player.getProfile().getName());
    }

    /**
     * Map the given username to an alias.
     * Remapped usernames are consistent across restarts, but they are based on the value of the string.
     * Thus, a player who changes username will get a fresh mapped name.
     * @param playerName the username of the player to rename.
     * @return a new name to be used instead of their actual name.
     */
    public static String tryRenamePlayerName(String playerName) {
        if (IncognitoState.NAME_MAP.containsKey(playerName)) {
            return IncognitoState.NAME_MAP.get(playerName);
        } else {
            String newName = IncognitoState.NAME_POOL.get(0);
            IncognitoState.NAME_MAP.put(playerName, newName);
            IncognitoState.NAME_POOL.remove(newName);
            return newName;
        }
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
            player.setTabListDisplayName(new TextComponent(tryRenamePlayer(player)).withStyle(ChatFormatting.BLUE));
        });
    }

    /**
     * Given an incoming chat message, mutate it to replace user names.
     * TODO: Make changed names highlight in Blue.
     * @param event the ClientChatReceivedEvent instance that signals an incoming message.
     */
    public static void renameChatMessage(ClientChatReceivedEvent event) {
        if (!IncognitoState.ENABLED) return;

        if (event.getMessage() instanceof TranslatableComponent translatable) {
            // Arg 0 = TextComponent containing player name
            // Arg 1 = String containing chat message
            Object[] args = translatable.getArgs();
            String messageAuthorName = ((TextComponent) args[0]).getSiblings().get(0).getContents();
            String messageContent = (String) args[1], originalContent = (String) args[1];

            for (PlayerInfo player : userListCache) {
                String playerName = player.getProfile().getName();
                if (messageContent.toLowerCase(Locale.ROOT).contains(playerName.toLowerCase(Locale.ROOT))) {
                    messageContent = messageContent.replaceAll(playerName, tryRenamePlayerName(playerName));
                }
            }

            if (!messageContent.equals(originalContent)) {
                // Save message content
                args[1] = messageContent;
                // parts: "<", name, ">", message
                List<FormattedText> parts = ObfuscationReflectionHelper.getPrivateValue(TranslatableComponent.class, translatable, "f_131301" + "_");
                List<FormattedText> newParts = new ArrayList<>(parts);
                newParts.set(3, new TextComponent(messageContent).withStyle(ChatFormatting.AQUA));

                ObfuscationReflectionHelper.setPrivateValue(TranslatableComponent.class, translatable, newParts, "f_131301" + "_");
            }

            // Rename player
            messageAuthorName = tryRenamePlayerName(messageAuthorName);

            // Save player name
            Component playerName = ((TextComponent) args[0]).getSiblings().get(0);
            TextComponent newMessage = new TextComponent(messageAuthorName);
            newMessage.withStyle(playerName.getStyle());
            newMessage.withStyle(ChatFormatting.AQUA);

            ((TextComponent) args[0]).getSiblings().set(0, newMessage);
        }
    }

    /**
     * Given an entity nameplate attempting to render, mutate it to replace the username.
     * TODO: Make changed names highlight in Blue.
     * @param event the RenderNameplateEvent instance that signals a nameplate render.
     */
    public static void renameNamePlate(RenderNameplateEvent event) {
        if (!IncognitoState.ENABLED) return;

        String name = event.getContent().getContents();
        name = tryRenamePlayerName(name);

        TextComponent newMessage = new TextComponent(name);
        newMessage.withStyle(event.getContent().getStyle());
        event.getContent().getSiblings().forEach(newMessage::append);

        event.setContent(newMessage);
    }
}
