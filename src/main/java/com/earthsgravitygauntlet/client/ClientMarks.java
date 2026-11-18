package com.earthsgravitygauntlet.client;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Client-side cache of marked entity ids (synced from the server). Pure Java so it loads on any side. */
public final class ClientMarks {
    private static volatile Set<Integer> ids = Set.of();

    private ClientMarks() {}

    public static void set(List<Integer> newIds) {
        ids = new HashSet<>(newIds);
    }

    public static boolean contains(int id) {
        return ids.contains(id);
    }
}
