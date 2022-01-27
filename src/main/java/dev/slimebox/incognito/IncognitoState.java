package dev.slimebox.incognito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hold global state; information about how Incognito should run.
 * Contains things like the user to user map, and whether Incognito is even enabled right now.
 * Certain things can be loaded from static configuration also, such as the component modifier for mapped names.
 *
 * @author Curle
 */
public final class IncognitoState {
    IncognitoState() {}

    /******* RUNTIME STATE *********/

    /*
     Whether Incognito is currently enabled.
     Will show a status indicator in the top-right of the screen.
     Will enable Component transformation also.
    */
    public static boolean ENABLED = false;

    /******* STATIC STATE **********/

    /*
     List of names that can be chosen.
     The Name Map's entries will be a subset of this pool.
     Names can be chosen at random.
     They must be removed from the pool when chosen.
     */
    public static List<String> NAME_POOL = new ArrayList<>();

    /*
     Mapping of Player names -> Incognito names.
     This map shall take names from the pool and allocate them to an in-game player.
     The map shall then be saved to disk, and cached for future startups.
     The map can also be viewed and edited by the player in-game.
     */
    public static Map<String, String> NAME_MAP = new HashMap<>();
}
