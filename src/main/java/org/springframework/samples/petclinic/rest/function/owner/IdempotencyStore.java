package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

/**
 * Remembers which owner a create request with a given {@code Idempotency-Key} produced, so a repeat
 * of that create (same key) returns the originally created owner instead of creating a duplicate.
 *
 * <p>The mapping is key -&gt; owner id; it is process-wide (a Spring singleton) and holds only the
 * id, so the current owner state is always read back fresh from the repository on replay.
 */
@Component
public class IdempotencyStore {

    private final ConcurrentMap<String, Integer> keyToOwnerId = new ConcurrentHashMap<>();

    /** Owner id previously created under {@code key}, or {@code null} when the key is new. */
    public Integer find(String key) {
        return keyToOwnerId.get(key);
    }

    /** Record that {@code key} created the owner with {@code ownerId} (first writer wins). */
    public void record(String key, Integer ownerId) {
        keyToOwnerId.putIfAbsent(key, ownerId);
    }
}
