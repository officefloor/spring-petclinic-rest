package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

/**
 * Remembers which {@code Idempotency-Key} header values have already created an owner, mapping each
 * seen key to the id of the owner it created.
 *
 * <p>Used by the {@code POST /api/owners} pipeline to make a create idempotent: {@link
 * CheckOwnerIdempotency} consults this store first and, when a key has already been seen, replays the
 * originally created owner (200) instead of creating a duplicate; {@link RecordOwnerIdempotency}
 * populates it after a fresh owner is persisted. Keys are supplied by the caller and are opaque.
 */
@Component
public class OwnerIdempotencyStore {

    private final ConcurrentMap<String, Integer> keyToOwnerId = new ConcurrentHashMap<>();

    /** The id of the owner previously created under {@code key}, or {@code null} if the key is new. */
    public Integer find(String key) {
        return this.keyToOwnerId.get(key);
    }

    /** Remember that {@code key} created the owner with the given id. */
    public void record(String key, Integer ownerId) {
        this.keyToOwnerId.putIfAbsent(key, ownerId);
    }
}
