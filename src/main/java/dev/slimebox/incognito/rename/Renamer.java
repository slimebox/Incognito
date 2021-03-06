package dev.slimebox.incognito.rename;

import com.mojang.authlib.GameProfile;
import dev.slimebox.incognito.Incognito;
import dev.slimebox.incognito.IncognitoState;
import dev.slimebox.incognito.api.IncognitoAPI;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderNameplateEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.*;
import java.util.stream.Collectors;


/**
 * Handles the allocation and fetching of user map renames.
 * Usernames are string-based, not UUID based.
 * Thus, a user who changes username will be allocated a new entry in the map.
 *
 * @author Curle
 */
public final class Renamer implements IncognitoAPI {
    static private List<GameProfile> userListCache = new ArrayList<>();
    static private List<PlayerInfo> fixedPlayers = new ArrayList<>();

    /**
     * Fetch (and allocate, if it doesn't already exist) a name for the given player.
     */
    @Override
    public String remapPlayer(GameProfile player) {
        return remapPlayerName(player.getName());
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
        if (userListCache.isEmpty()) updateCaches();
        if (userListCache.isEmpty()) return text;

        for (GameProfile player : userListCache) {
            String playerName = player.getName();
            if (text.toLowerCase(Locale.ROOT).contains(playerName.toLowerCase(Locale.ROOT))) {
                text = text.replaceAll(playerName, Incognito.API.remapPlayerName(playerName));
            }
        }

        return text;
    }

    @Override
    public Component remapComponent(Component input) {
        if (userListCache.isEmpty()) updateCaches();
        if (userListCache.isEmpty()) return input;

        // Remap siblings recursively
        List<Component> siblings = input.getSiblings();
        for(int i = 0; i < siblings.size(); i++) {
            siblings.set(i, remapComponent(siblings.get(i)));
        }

        // If there's a text component, copy and edit
        // TODO: this will lose all prior formatting
        if (input instanceof TextComponent text) {
            for (GameProfile player : userListCache) {
                String msg = text.getText(), name = player.getName();
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

                    return remapped;
                }

            }
        }
        if (input instanceof TranslatableComponent translatable) {
            // parts: "<", name, ">", message
            List<FormattedText> parts = ObfuscationReflectionHelper.getPrivateValue(TranslatableComponent.class, translatable, "f_131301" + "_");
            List<FormattedText> newParts = new ArrayList<>(parts);

            // Server /say messages use "[%s] %s", so we need to transform args too.
            Object[] args = translatable.getArgs();
            // Need to index by number, so use a traditional loop
            for (int i = 0; i < args.length; i++ ) {
                Object text = args[i];
                if (text instanceof Component component) {
                    args[i] = remapComponent(component);
                } else if (text instanceof String str){
                    args[i] = remapComponent(new TextComponent(str));
                }
            }

            // Need to index by number, so use a traditional loop here too
            for (int i = 0; i < newParts.size(); i++ ) {
                FormattedText text = newParts.get(i);
                if (text instanceof Component component) {
                    newParts.set(i, remapComponent(component));
                } else {
                    newParts.set(i, remapComponent(new TextComponent(text.getString())));
                }
            }
            ObfuscationReflectionHelper.setPrivateValue(TranslatableComponent.class, translatable, newParts, "f_131301" + "_");

            return translatable;
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
        if (userListCache.isEmpty()) updateCaches();
        if (userListCache.isEmpty()) return;

        ClientPacketListener clientpacketlistener = Minecraft.getInstance().getConnection();
        var localList = new ArrayList<>(clientpacketlistener.getOnlinePlayers());
        // Guarantee the current user is in the list no matter what
        localList.forEach(player -> {
            if (!fixedPlayers.contains(player)) {
                player.setTabListDisplayName(new TextComponent(Incognito.API.remapPlayer(player.getProfile())).withStyle(ChatFormatting.BLUE));
                fixedPlayers.add(player);
            }
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

    public static void updateCaches() {
        if (!IncognitoState.ENABLED) return;

        // If we're connected to integrated server..
        if (Minecraft.getInstance().hasSingleplayerServer()) {
            var list = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers();
            userListCache = new ArrayList<>();
            // Fill cache from the server list
            for (ServerPlayer player : list)
                userListCache.add(player.getGameProfile());
        // Otherwise, we must be connected to a remote server.
        } else if (Minecraft.getInstance().player != null) {
            var list = Minecraft.getInstance().getConnection().getOnlinePlayers();
            userListCache = new ArrayList<>();
            for (PlayerInfo info : list)
                userListCache.add(info.getProfile());
        }
    }

}
