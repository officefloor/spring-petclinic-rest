package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

/**
 * Remembers which owner a given {@code Idempotency-Key} created, so a repeated create carrying an
 * already-seen key returns the original owner (200) instead of creating a duplicate.
 *
 * <p>The mapping is deliberately kept outside the request transaction (a plain in-memory map, not a
 * persisted entity): the key is a client-supplied de-duplication token, not domain data, and must
 * survive independently of whether any individual create commits or rolls back.
 */
@Component
public class IdempotencyStore {

    private final ConcurrentMap<String, Integer> keyToOwnerId = new ConcurrentHashMap<>();

    /** Owner id previously created for {@code key}, or {@code null} if the key has not been seen. */
    public Integer find(String key) {
        return this.keyToOwnerId.get(key);
    }

    /** Record that {@code key} created the owner with {@code ownerId}. */
    public void record(String key, Integer ownerId) {
        this.keyToOwnerId.putIfAbsent(key, ownerId);
    }
}
