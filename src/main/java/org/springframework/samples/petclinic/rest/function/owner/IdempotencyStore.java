package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Remembers which owner a create request produced, keyed by the client-supplied
 * {@code Idempotency-Key}. A repeat create carrying an already-seen key resolves back to the
 * originally created owner (see {@link CheckIdempotencyKey}) instead of creating a duplicate.
 *
 * <p>Blank or absent keys are never recorded and never match: an absent key means the client
 * did not opt in to idempotency, so those requests follow the ordinary create path.
 */
@Component
public class IdempotencyStore {

    private final Map<String, Integer> ownerIdByKey = new ConcurrentHashMap<>();

    /** The owner id previously created under {@code key}, or {@code null} for a new or blank key. */
    public Integer find(String key) {
        return (key == null || key.isBlank()) ? null : ownerIdByKey.get(key);
    }

    /** Record that {@code key} created the owner with {@code ownerId}. A no-op for a blank key. */
    public void record(String key, int ownerId) {
        if (key != null && !key.isBlank()) {
            ownerIdByKey.putIfAbsent(key, ownerId);
        }
    }
}
