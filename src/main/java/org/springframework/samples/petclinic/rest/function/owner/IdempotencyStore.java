package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Remembers which owner each {@code Idempotency-Key} first created, so a repeated create
 * with an already-seen key can return the original owner instead of making a duplicate.
 * Keys map to the created owner's id; the owner itself is reloaded on replay so no detached
 * entity is held across requests. Thread-safe and application-scoped.
 */
@Component
public class IdempotencyStore {

    private final Map<String, Integer> ownerIdByKey = new ConcurrentHashMap<>();

    /** The id of the owner first created under {@code key}, or {@code null} if unseen. */
    public Integer find(String key) {
        return this.ownerIdByKey.get(key);
    }

    /** Records that {@code key} created the given owner, keeping the first winner on a race. */
    public void record(String key, int ownerId) {
        this.ownerIdByKey.putIfAbsent(key, ownerId);
    }
}
