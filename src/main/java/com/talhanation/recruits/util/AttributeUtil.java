package com.talhanation.recruits.util;

import com.talhanation.recruits.Main;
import net.minecraft.resources.Identifier;

import java.util.Locale;
import java.util.UUID;

public final class AttributeUtil {

    private AttributeUtil() {
    }

    /**
     * Attribute modifiers are keyed by id now. The old code created a new random UUID for
     * every modifier so that bonuses stack; keep that behaviour with a unique id per call.
     */
    public static Identifier uniqueId(String name) {
        return Identifier.fromNamespaceAndPath(Main.MOD_ID, name.toLowerCase(Locale.ROOT) + "_" + UUID.randomUUID());
    }
}
