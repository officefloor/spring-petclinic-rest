package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Remembers which owner was created for a given {@code Idempotency-Key}. Keyed purely on
 * the header value: a create that repeats with an already-seen key is served the id of the
 * owner created the first time, rather than creating a duplicate.
 *
 * <p>Process-wide and thread-safe, so it spans the separate requests of a retry.
 */
@Component
public class OwnerIdempotencyStore {

    private final Map<String, Integer> ownerIdByKey = new ConcurrentHashMap<>();

    /** The owner id already created for {@code key}, or {@code null} if the key is new. */
    public Integer find(String key) {
        return (key == null) ? null : this.ownerIdByKey.get(key);
    }

    /** Record that {@code key} created the owner with {@code ownerId} (no-op for a null key). */
    public void record(String key, Integer ownerId) {
        if (key != null && ownerId != null) {
            this.ownerIdByKey.putIfAbsent(key, ownerId);
        }
    }
}
