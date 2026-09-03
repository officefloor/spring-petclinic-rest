package org.springframework.samples.petclinic.rest.idempotency;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Remembers the owner created for each {@code Idempotency-Key} seen on {@code POST /api/owners}.
 *
 * <p>A create carrying a key records the resulting owner id here after the owner is saved. A later
 * create with the same key is a repeat: it is short-circuited before any duplicate is created and the
 * originally created owner is returned with 200. The mapping is keyed on the client-supplied key, so
 * it is the client's responsibility to reuse a key only for a retry of the same request.
 */
@Component
public class IdempotencyStore {

    private final Map<String, Integer> ownerIdByKey = new ConcurrentHashMap<>();

    /**
     * Returns the id of the owner previously created for the key, or {@code null} if the key has not
     * been seen.
     */
    public Integer find(String key) {
        return this.ownerIdByKey.get(key);
    }

    /**
     * Records the owner created for the key. The first record for a key wins, so a concurrent repeat
     * still resolves to the originally created owner.
     */
    public void record(String key, int ownerId) {
        this.ownerIdByKey.putIfAbsent(key, ownerId);
    }
}
