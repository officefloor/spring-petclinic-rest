package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

/**
 * Remembers which {@link org.springframework.samples.petclinic.model.Owner} a create request with a
 * given {@code Idempotency-Key} produced, so that a repeat of the same create (same key) returns the
 * originally created owner instead of creating a duplicate.
 *
 * <p>Keys are recorded by {@link RecordIdempotencyKey} once the owner is saved and read back by
 * {@link CheckIdempotencyKey} at the start of the create pipeline. A Spring singleton, so the mapping
 * survives across requests.
 */
@Component
public class IdempotencyStore {

    private final ConcurrentMap<String, Integer> ownerIdByKey = new ConcurrentHashMap<>();

    /** The id of the owner previously created for {@code key}, or {@code null} if the key is new. */
    public Integer find(String key) {
        return this.ownerIdByKey.get(key);
    }

    /** Record that {@code key} created the owner with {@code ownerId} (first writer wins). */
    public void record(String key, Integer ownerId) {
        this.ownerIdByKey.putIfAbsent(key, ownerId);
    }
}
