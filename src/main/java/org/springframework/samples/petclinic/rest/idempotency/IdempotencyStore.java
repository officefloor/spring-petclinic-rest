package org.springframework.samples.petclinic.rest.idempotency;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

/**
 * Remembers which owner each {@code Idempotency-Key} created, so a create that repeats with an
 * already-seen key can return the originally created owner instead of creating a duplicate.
 *
 * <p>Keyed on the raw header value; the value is the persisted owner id. A process-wide singleton
 * (not transactional) so the mapping outlives the request that recorded it.
 */
@Component
public class IdempotencyStore {

    private final ConcurrentMap<String, Integer> ownerIdByKey = new ConcurrentHashMap<>();

    /** The owner id previously created under {@code key}, or {@code null} if the key is new. */
    public Integer find(String key) {
        return ownerIdByKey.get(key);
    }

    /** Record that {@code key} created the owner with {@code ownerId}. */
    public void record(String key, Integer ownerId) {
        ownerIdByKey.put(key, ownerId);
    }
}
