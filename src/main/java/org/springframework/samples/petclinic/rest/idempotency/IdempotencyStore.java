package org.springframework.samples.petclinic.rest.idempotency;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

/**
 * Remembers which resource an {@code Idempotency-Key} first created, so a create that repeats
 * with an already-seen key can return the original resource instead of creating a duplicate.
 *
 * <p>Keyed by the client-supplied header value; the stored value is the created owner's id.
 * A single shared, thread-safe map: the first create for a key wins and its id is pinned.
 */
@Component
public class IdempotencyStore {

    private final ConcurrentMap<String, Integer> createdIdByKey = new ConcurrentHashMap<>();

    /** The id created for {@code key}, if a create with that key has already completed. */
    public Optional<Integer> find(String key) {
        return Optional.ofNullable(this.createdIdByKey.get(key));
    }

    /** Pin {@code key} to the id it created; the first create for a key wins. */
    public void record(String key, int createdId) {
        this.createdIdByKey.putIfAbsent(key, createdId);
    }
}
