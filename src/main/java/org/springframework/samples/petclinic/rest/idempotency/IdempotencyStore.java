package org.springframework.samples.petclinic.rest.idempotency;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

/**
 * Remembers which owner was created for a given {@code Idempotency-Key}, so a create that repeats
 * with an already-seen key can return the originally created owner instead of creating a duplicate.
 *
 * <p>Keyed on the raw header value; the mapped value is the persisted owner's id. Held in memory
 * for the lifetime of the application — the guarantee is "retry-safe within this instance", not
 * durable across restarts.
 */
@Component
public class IdempotencyStore {

    private final ConcurrentMap<String, Integer> ownerIdByKey = new ConcurrentHashMap<>();

    /** The id of the owner already created for this key, or {@code null} when the key is new. */
    public Integer find(String key) {
        return ownerIdByKey.get(key);
    }

    /** Remember that this key created the owner with the given id. */
    public void record(String key, Integer ownerId) {
        ownerIdByKey.putIfAbsent(key, ownerId);
    }
}
