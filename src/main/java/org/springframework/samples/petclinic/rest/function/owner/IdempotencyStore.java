package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Records which {@code Idempotency-Key} header values have already produced an owner on
 * {@code POST /api/owners}, mapping each seen key to the id of the owner it originally created.
 *
 * <p>Used by {@link CheckIdempotency} (to detect a repeat before any work is done) and
 * {@link RecordIdempotency} (to remember a key once its owner has been persisted). A Spring
 * {@code @Component} so it is a process-wide singleton injected by type into those steps.
 */
@Component
public class IdempotencyStore {

    private final ConcurrentHashMap<String, Integer> keyToOwnerId = new ConcurrentHashMap<>();

    /** The id of the owner previously created under {@code key}, or {@code null} if never seen. */
    public Integer find(String key) {
        return (key == null) ? null : this.keyToOwnerId.get(key);
    }

    /** Remember that {@code key} created the owner with {@code ownerId}; the first winner stays. */
    public void record(String key, Integer ownerId) {
        if (key != null && ownerId != null) {
            this.keyToOwnerId.putIfAbsent(key, ownerId);
        }
    }
}
