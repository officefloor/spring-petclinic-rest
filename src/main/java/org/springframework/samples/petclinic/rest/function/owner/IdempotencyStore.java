package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Remembers which {@code Idempotency-Key} values have already produced an owner, mapping each
 * seen key to the id of the owner it originally created. Shared singleton so the mapping
 * survives across requests. A repeated create carrying an already-seen key can therefore return
 * the originally created owner instead of creating a duplicate.
 */
@Component
public class IdempotencyStore {

    private final Map<String, Integer> keyToOwnerId = new ConcurrentHashMap<>();

    /** The id of the owner previously created under {@code key}, or {@code null} if unseen. */
    public Integer find(String key) {
        return this.keyToOwnerId.get(key);
    }

    /** Records that {@code key} created the owner with {@code ownerId} (first write wins). */
    public void record(String key, int ownerId) {
        this.keyToOwnerId.putIfAbsent(key, ownerId);
    }
}
