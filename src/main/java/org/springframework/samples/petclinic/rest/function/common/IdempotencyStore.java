package org.springframework.samples.petclinic.rest.function.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Remembers which entity a completed create produced for a given {@code Idempotency-Key}, so a
 * repeat of that create (same key) can return the originally created entity instead of creating a
 * duplicate.
 *
 * <p>An application-scoped singleton keyed on the client-supplied key; the stored value is the
 * generated id of the created entity. Kept intentionally simple (an in-memory map) — the mapping
 * only needs to outlive the individual requests that share a key.
 */
@Component
public class IdempotencyStore {

    private final Map<String, Integer> keyToId = new ConcurrentHashMap<>();

    /** The id previously created under {@code key}, or {@code null} if the key has not been seen. */
    public Integer lookup(String key) {
        return keyToId.get(key);
    }

    /** Records that {@code key} produced the entity with {@code id}. */
    public void record(String key, Integer id) {
        keyToId.put(key, id);
    }
}
