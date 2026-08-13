package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

/**
 * Remembers the id of the owner created for each {@code Idempotency-Key} seen on
 * {@code POST /api/owners}. A repeated create carrying an already-seen key is answered from this
 * map (see {@link CheckIdempotencyKey}) instead of creating a duplicate. A singleton bean, so the
 * mapping is shared across requests.
 */
@Component
public class IdempotencyStore {

    private final ConcurrentMap<String, Integer> keyToOwnerId = new ConcurrentHashMap<>();

    /** The owner id previously created for {@code key}, or {@code null} if the key is unseen. */
    public Integer find(String key) {
        return (key == null) ? null : this.keyToOwnerId.get(key);
    }

    /** Record that {@code key} created the owner with {@code ownerId}. First writer wins. */
    public void record(String key, int ownerId) {
        if (key != null && !key.isBlank()) {
            this.keyToOwnerId.putIfAbsent(key, ownerId);
        }
    }
}
