package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Process-wide registry mapping a {@code POST /api/owners} request's {@code Idempotency-Key} to the
 * id of the owner that request created. A later create repeating an already-recorded key is answered
 * with the originally created owner instead of creating a duplicate.
 */
@Component
public class IdempotencyStore {

    private final Map<String, Integer> keyToOwnerId = new ConcurrentHashMap<>();

    /** The owner id recorded for {@code key}, or {@code null} when the key is unknown or {@code null}. */
    public Integer find(String key) {
        return key == null ? null : keyToOwnerId.get(key);
    }

    /** Record {@code ownerId} against {@code key}; a no-op when either is {@code null}. */
    public void record(String key, Integer ownerId) {
        if (key != null && ownerId != null) {
            keyToOwnerId.put(key, ownerId);
        }
    }
}
