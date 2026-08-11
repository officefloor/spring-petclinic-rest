package org.springframework.samples.petclinic.rest.support;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

/**
 * Application-wide record of which {@code Idempotency-Key} headers have already produced a
 * created owner. A create that repeats with a key already seen must return the originally
 * created owner instead of creating a duplicate, so the mapping outlives any single request
 * and is shared across threads.
 *
 * <p>Keyed by the raw header value, mapping to the id of the owner the first request created.
 * Population is deliberately once-only: {@link #putIfAbsent(String, int)} keeps the first
 * winner so a concurrent retry cannot overwrite the original id.
 */
@Component
public class IdempotencyStore {

    private final ConcurrentMap<String, Integer> ownerIdByKey = new ConcurrentHashMap<>();

    /** The owner id previously created for {@code key}, or {@code null} if the key is unseen. */
    public Integer find(String key) {
        return key == null ? null : ownerIdByKey.get(key);
    }

    /** Remember that {@code key} created {@code ownerId}; the first value recorded wins. */
    public void putIfAbsent(String key, int ownerId) {
        if (key != null) {
            ownerIdByKey.putIfAbsent(key, ownerId);
        }
    }
}
