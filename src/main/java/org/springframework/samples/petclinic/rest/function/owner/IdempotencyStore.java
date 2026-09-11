package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

/**
 * Remembers which owner a create request produced for a given {@code Idempotency-Key},
 * so a repeat of that create returns the originally created owner instead of making a
 * duplicate. Injected by type into the create pipeline's idempotency steps
 * ({@link CheckIdempotencyKey} records the decision, {@link RecordIdempotencyKey} stores
 * the mapping once the owner has an id).
 *
 * <p>The mapping is keyed on the raw header value and holds only the created owner's id;
 * the owner itself is re-read from the repository when a repeat is served, so the response
 * always reflects the current stored state.
 */
@Component
public class IdempotencyStore {

    private final ConcurrentMap<String, Integer> ownerIdByKey = new ConcurrentHashMap<>();

    /** The id of the owner previously created under {@code key}, or {@code null} if unseen. */
    public Integer find(String key) {
        return this.ownerIdByKey.get(key);
    }

    /** Remember that {@code key} produced the owner with {@code ownerId}. */
    public void record(String key, int ownerId) {
        this.ownerIdByKey.putIfAbsent(key, ownerId);
    }
}
