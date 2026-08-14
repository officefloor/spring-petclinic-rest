package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

/**
 * Remembers which owner id was created for a given {@code Idempotency-Key}, so a repeated create with
 * an already-seen key can return the original owner (200) instead of creating a duplicate.
 *
 * <p>A process-wide, in-memory map: keys are chosen by the client and are expected to be unique per
 * logical create, so entries never need eviction for correctness. Used by {@code CheckIdempotentCreate}
 * (lookup) and {@code RecordIdempotentCreate} (store, once the owner has an id).
 */
@Component
public class IdempotencyStore {

    private final ConcurrentMap<String, Integer> keyToOwnerId = new ConcurrentHashMap<>();

    /** The owner id previously created for this key, or {@code null} if the key has not been seen. */
    public Integer find(String key) {
        return (key == null) ? null : this.keyToOwnerId.get(key);
    }

    /** Record that {@code key} created the owner with {@code ownerId} (first writer wins). */
    public void record(String key, int ownerId) {
        if (key != null) {
            this.keyToOwnerId.putIfAbsent(key, ownerId);
        }
    }
}
