package org.springframework.samples.petclinic.rest.function.common;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

/**
 * Process-wide registry of {@code Idempotency-Key} header values to the id of the owner that the
 * first request carrying that key created. Injected as a Spring bean (by type) into the create-owner
 * pipeline so a repeated create with an already-seen key can return the original owner instead of
 * creating a duplicate.
 *
 * <p>Keyed on the raw header value; a request without the header is not recorded and never replays.
 */
@Component
public class IdempotencyStore {

    private final ConcurrentMap<String, Integer> keyToOwnerId = new ConcurrentHashMap<>();

    /** The owner id previously created under {@code key}, or {@code null} if the key is new. */
    public Integer find(String key) {
        return (key == null || key.isBlank()) ? null : keyToOwnerId.get(key);
    }

    /** Remember that {@code key} created the owner with {@code ownerId}. First writer wins. */
    public void record(String key, Integer ownerId) {
        if (key != null && !key.isBlank() && ownerId != null) {
            keyToOwnerId.putIfAbsent(key, ownerId);
        }
    }
}
