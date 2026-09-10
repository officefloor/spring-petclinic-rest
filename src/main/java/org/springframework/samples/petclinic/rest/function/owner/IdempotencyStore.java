package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

/**
 * Remembers the owner created for a given {@code Idempotency-Key} so that a repeated
 * create carrying an already-seen key returns the originally created owner instead of
 * creating a duplicate. Keys are opaque and supplied by the caller; the mapping is held
 * for the lifetime of the application, independent of the request transaction, so a
 * committed create stays idempotent for later repeats.
 */
@Component
public class IdempotencyStore {

    private final ConcurrentMap<String, Integer> ownerIdByKey = new ConcurrentHashMap<>();

    /** Owner id previously created for {@code key}, or {@code null} if the key is unseen. */
    public Integer find(String key) {
        return this.ownerIdByKey.get(key);
    }

    /** Record that {@code key} created the owner with the given id. */
    public void record(String key, int ownerId) {
        this.ownerIdByKey.putIfAbsent(key, ownerId);
    }
}
