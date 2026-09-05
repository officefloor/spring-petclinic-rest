package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

/**
 * Remembers which owner a given {@code Idempotency-Key} first created, so a repeated
 * {@code POST /api/owners} carrying an already-seen key replays the original owner (200)
 * instead of creating a duplicate (which the identity rules would otherwise reject 409).
 *
 * <p>A process-wide Spring singleton (not tied to any request or database transaction):
 * {@link CheckIdempotencyKey} consults it as the first step of the create pipeline, and
 * {@link RecordIdempotencyKey} populates it once the owner has been saved and has an id.
 * The mapping is key &rarr; persisted owner id; the replay reloads the owner from the
 * repository so the response is built from the current stored state.
 */
@Component
public class IdempotencyStore {

    private final ConcurrentMap<String, Integer> keyToOwnerId = new ConcurrentHashMap<>();

    /** The owner id previously created under {@code key}, or {@code null} if the key is unseen. */
    public Integer find(String key) {
        return key == null ? null : keyToOwnerId.get(key);
    }

    /** Remembers that {@code key} created the owner with {@code ownerId} (first writer wins). */
    public void record(String key, Integer ownerId) {
        if (key != null && ownerId != null) {
            keyToOwnerId.putIfAbsent(key, ownerId);
        }
    }
}
