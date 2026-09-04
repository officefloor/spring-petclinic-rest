package org.springframework.samples.petclinic.rest.support;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

/**
 * Remembers the owner id created for each seen {@code Idempotency-Key}, so a create that
 * repeats with an already-seen key can return the originally created owner instead of
 * creating a duplicate. Process-local and unbounded — adequate for the single create
 * endpoint that consults it.
 */
@Component
public class IdempotencyStore {

    private final ConcurrentMap<String, Integer> keyToOwnerId = new ConcurrentHashMap<>();

    /** The owner id previously created for {@code key}, or {@code null} if the key is unseen. */
    public Integer find(String key) {
        return keyToOwnerId.get(key);
    }

    /** Record that {@code key} created the owner with {@code ownerId} (first writer wins). */
    public void record(String key, int ownerId) {
        keyToOwnerId.putIfAbsent(key, ownerId);
    }
}
