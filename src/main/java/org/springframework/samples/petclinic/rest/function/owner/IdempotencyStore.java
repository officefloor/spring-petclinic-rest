package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Remembers which owner was created for each {@code Idempotency-Key} seen on
 * {@code POST /api/owners}, so a repeated create carrying an already-seen key returns the
 * originally created owner instead of creating a duplicate.
 *
 * <p>A process-wide singleton (Spring bean) keyed by the client-supplied key; the mapping
 * is to the created owner's id, which the replay path re-loads from the repository.
 */
@Component
public class IdempotencyStore {

    private final Map<String, Integer> keyToOwnerId = new ConcurrentHashMap<>();

    /** The owner id previously created for {@code key}, or {@code null} if the key is unseen. */
    public Integer find(String key) {
        return key == null ? null : this.keyToOwnerId.get(key);
    }

    /** Remember the owner created for {@code key}. The first create for a key wins. */
    public void record(String key, Integer ownerId) {
        if (key != null && ownerId != null) {
            this.keyToOwnerId.putIfAbsent(key, ownerId);
        }
    }
}
