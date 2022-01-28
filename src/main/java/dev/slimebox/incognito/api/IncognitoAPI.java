package dev.slimebox.incognito.api;

import dev.slimebox.incognito.Incognito;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

/**
 * Public interface for accessing Incognito features.
 * The Player Name map can be accessed by compiling against the full Incognito mod.
 *
 * However, this api-only class can be used in lieu.
 *
 * @author Curle
 */
public interface IncognitoAPI {

    /**
     * Get the instance of this API that you need to run the below methods.
     * This function does nothing in the API distribution, but it has content in the full mod.
     * Thus, this API does nothing to your environment if you run it without the full mod.
     */
    static IncognitoAPI getAPI() { return Incognito.API; }

    /**
     * If you wish to add a name that players can potentially be named, you may do so here.
     * @param name the new name to add.
     */
    void addPlayerNameToPool(String name);

    /**
     * Process a player's username string.
     * This must be ONLY the username, and it must be trimmed and stripped.
     * @param playerName the username to convert
     * @return the mapped username to use instead.
     */
    String remapPlayerName(String playerName);

    /**
     * Process a player's connection data.
     * @param player the data of the player to rename
     * @return the mapped username to use in place of their actual name
     */
    String remapPlayer(PlayerInfo player);

    /**
     * Process a block of text that may or may not contain a player name.
     * Every instance of a player name, non-case-sensitive, will be replaced.
     * This is raw string replacement, prefer the Component version instead.
     *
     * This does not modify in-place, a modified copy will be returned.
     *
     * Note that this can lead to weird substitutions;
     *  "It's time for Development" -> "It's time for cheezey_tigerelopment"
     *
     * @param text the raw text to process.
     * @return the raw text with all instances of player usernames remapped.
     */
    String remapText(String text);

    /**
     * Process a component that may or may not contain a player name.
     * Every instance of a player name, non-case-sensitive, will be replaced.
     * This component will be searched recursively, so time complexity will be high.
     *
     * Everywhere a match is found, the replaced text will be inserted with an AQUA formatting.
     *
     * Note that this can lead to weird substitutions;
     *  "It's time for Development" -> "It's time for cheezey_tigerelopment"
     *
     * @param input the Component to process.
     * @return the Component with all instances of player usernames remapped.
     */
    Component remapComponent(Component input);
}
