package org.springframework.samples.petclinic.rest.support;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

/**
 * Process-wide record of the owners created under a given {@code Idempotency-Key}.
 *
 * <p>The {@code POST /api/owners} pipeline consults this before creating: a repeat request
 * carrying a key already seen resolves to the originally created owner id (returned with 200)
 * instead of creating a duplicate. Keys are stored only after the create succeeds.
 *
 * <p>Injected by type into the create functions (see
 * {@code CheckIdempotencyKey} / {@code RecordIdempotencyKey}). The map is a simple in-memory
 * cache — deliberately independent of the request transaction, so a committed create's key is
 * visible to later requests.
 */
@Component
public class IdempotencyStore {

    private final ConcurrentMap<String, Integer> keyToOwnerId = new ConcurrentHashMap<>();

    /** The owner id previously created under {@code key}, or {@code null} if none. */
    public Integer find(String key) {
        return key == null ? null : keyToOwnerId.get(key);
    }

    /** Remember that {@code key} created the owner with {@code ownerId} (no-op for a blank key). */
    public void record(String key, int ownerId) {
        if (key != null && !key.isBlank()) {
            keyToOwnerId.putIfAbsent(key, ownerId);
        }
    }
}
